package com.example.checka2.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.checka2.data.AppDatabase
import com.example.checka2.data.MatchResult
import com.example.checka2.data.UserPreferences
import com.example.checka2.domain.CheckaAI
import com.example.checka2.domain.Difficulty
import com.example.checka2.domain.GameAction
import com.example.checka2.domain.GameEngine
import com.example.checka2.domain.GameMode
import com.example.checka2.domain.GameState
import com.example.checka2.domain.Orientation
import com.example.checka2.domain.Player
import com.example.checka2.domain.Position
import com.example.checka2.domain.Wall
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "checka-db"
    ).build()
    private val dao = db.matchDao()

    // Game Setup
    private var gameMode: GameMode = GameMode.PassAndPlay
    private var difficulty: Difficulty = Difficulty.Easy
    private var p1Name: String = "Player 1"
    private var p2Name: String = "Player 2"

    // Game Core State
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // UI Interaction State
    private val _isPlaceWallMode = MutableStateFlow(false)
    val isPlaceWallMode = _isPlaceWallMode.asStateFlow()

    private val _wallOrientation = MutableStateFlow(Orientation.Horizontal)
    val wallOrientation = _wallOrientation.asStateFlow()

    private val _previewWall = MutableStateFlow<Wall?>(null)
    val previewWall = _previewWall.asStateFlow()
    
    // Tutorial State
    private val _showTutorial = MutableStateFlow(false)
    val showTutorial = _showTutorial.asStateFlow()

    // Turn Assist
    private val _validMoves = MutableStateFlow<List<Position>>(emptyList())
    val validMoves = _validMoves.asStateFlow()

    init {
        viewModelScope.launch {
            UserPreferences.getTutorialSeen(application).collect { seen ->
                if (!seen) {
                    _showTutorial.value = true
                }
            }
        }
        // Init valid moves for initial state
        calculateValidMoves(_gameState.value)
    }

    fun onTutorialDismissed() {
        _showTutorial.value = false
        viewModelScope.launch {
            UserPreferences.setTutorialSeen(getApplication(), true)
        }
    }

    fun startGame(mode: GameMode, diff: Difficulty, name1: String, name2: String) {
        gameMode = mode
        difficulty = diff
        p1Name = name1
        p2Name = name2
        val initial = GameState()
        _gameState.value = initial
        calculateValidMoves(initial)
        _isPlaceWallMode.value = false
        _previewWall.value = null
    }

    fun onMoveSelected(pos: Position) {
        if (_isPlaceWallMode.value) return
        if (gameState.value.winner != null) return

        if (gameMode == GameMode.Solo && gameState.value.currentPlayer != Player.P1) return // AI Turn

        // CHECK VALIDITY via pre-calc list (Turn Assist)
        if (pos !in _validMoves.value) {
            _errorEvent.value = true
            return
        }

        val action = GameAction.Move(pos)
        val newState = GameEngine.applyAction(_gameState.value, action)
        
        if (newState != _gameState.value) {
            updateState(newState)
        } else {
            // Should not happen if in validMoves
            _errorEvent.value = true
        }
    }

    
    // Feedback
    private val _errorEvent = MutableStateFlow<Boolean>(false)

    fun onIntersectionSelected(row: Int, col: Int) {
        if (!_isPlaceWallMode.value) return
        if (gameMode == GameMode.Solo && gameState.value.currentPlayer != Player.P1) return

        val w = Wall(row, col, _wallOrientation.value)
        val isValid = GameEngine.isValidWallPlacement(w, gameState.value.walls, gameState.value.p1Pos, gameState.value.p2Pos)
        
        if (isValid) {
            if (_previewWall.value == w) {
                confirmWall()
            } else {
                _previewWall.value = w
            }
        } else {
            _previewWall.value = w // Invalid feedback
        }
    }
    
    fun toggleWallMode() {
        _isPlaceWallMode.update { !it }
        _previewWall.value = null
    }
    
    fun toggleOrientation() {
        _wallOrientation.update { if (it == Orientation.Horizontal) Orientation.Vertical else Orientation.Horizontal }
        _previewWall.value?.let { current ->
            val newO = if (current.orientation == Orientation.Horizontal) Orientation.Vertical else Orientation.Horizontal
            val newW = current.copy(orientation = newO)
            _previewWall.value = newW
        }
    }

    fun confirmWall() {
        val w = _previewWall.value ?: return
        val action = GameAction.PlaceWall(w)
        val newState = GameEngine.applyAction(_gameState.value, action)
        if (newState != _gameState.value) {
            updateState(newState)
            _isPlaceWallMode.value = false
            _previewWall.value = null
        }
    }

    private fun updateState(newState: GameState) {
        _gameState.value = newState
        checkWinner(newState)
        
        calculateValidMoves(newState) // Update highlights
        
        if (newState.winner == null && gameMode == GameMode.Solo && newState.currentPlayer == Player.P2) {
            triggerAITurn()
        }
    }
    
    private fun calculateValidMoves(state: GameState) {
        if (state.winner != null) {
            _validMoves.value = emptyList()
            return
        }
        _validMoves.value = GameEngine.getValidMoves(state)
    }
    
    // ... triggerAITurn, checkWinner ...
    
    private fun triggerAITurn() {
        viewModelScope.launch {
            delay(1000) // Thinking time
            if (_gameState.value.winner != null) return@launch
            
            val action = CheckaAI.chooseAction(_gameState.value, difficulty)
            val nextState = GameEngine.applyAction(_gameState.value, action)
            updateState(nextState)
        }
    }

    private fun checkWinner(state: GameState) {
        if (state.winner != null) {
            // Save result
            viewModelScope.launch {
                dao.insertMatch(
                    MatchResult(
                        mode = gameMode,
                        difficulty = if (gameMode == GameMode.Solo) difficulty.name else null,
                        player1Name = p1Name,
                        player2Name = p2Name,
                        winnerName = if (state.winner == Player.P1) p1Name else p2Name,
                        totalTurns = state.turnCount,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
