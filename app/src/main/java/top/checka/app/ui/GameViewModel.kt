package top.checka.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import top.checka.app.domain.CheckaAI
import top.checka.app.domain.Position
import top.checka.app.domain.Difficulty
import top.checka.app.domain.GameEngine
import top.checka.app.domain.GameMode
import top.checka.app.domain.Orientation
import top.checka.app.domain.Player
import top.checka.app.domain.Wall
import top.checka.app.domain.GameState
import top.checka.app.data.UserPreferences
import top.checka.app.data.MatchResult
import top.checka.app.data.AppDatabase
import top.checka.app.domain.GameAction
import androidx.room.Room
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "checka-db"
    ).build()
    private val dao = db.matchDao()

    // Game Setup
    private var gameMode: GameMode = GameMode.PassAndPlay
    private var difficulty: Difficulty = Difficulty.Easy
    
    // Dynamic Names
    private val _p1Name = MutableStateFlow("Player 1")
    val p1NameState = _p1Name.asStateFlow()
    
    private val _p2Name = MutableStateFlow("Player 2")
    val p2NameState = _p2Name.asStateFlow()

    // Game Core State
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _opponentData = MutableStateFlow<top.checka.app.data.api.OpponentData?>(null)
    val opponentData = _opponentData.asStateFlow()

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

    private val authRepository = top.checka.app.data.AuthRepository(application)
    private var currentUserId: String? = null
    private var gameStartTime: Long = 0L

    init {
        viewModelScope.launch {
            UserPreferences.getTutorialSeen(application).collect { seen ->
                if (!seen) {
                    _showTutorial.value = true
                }
            }
        }
        viewModelScope.launch {
            UserPreferences.getUserStats(application).collect { stats ->
                currentUserId = stats.userId
                _p1Avatar.value = stats.avatarUrl
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

    // Searching State
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun startGame(mode: GameMode, diff: Difficulty, name1: String, name2: String) {
        gameMode = mode
        difficulty = diff
        _p1Name.value = name1
        _p2Name.value = if (mode == GameMode.Ranked) "Searching..." else name2
        
        gameStartTime = System.currentTimeMillis()
        
        val initial = GameState()
        _gameState.value = initial
        calculateValidMoves(initial)
        _isPlaceWallMode.value = false
        _previewWall.value = null
        
        if (mode == GameMode.Ranked) {
             startRankedSearch()
        } else {
            _isSearching.value = false
        }
    }
    
    private var isGhostBot = false

    // Avatars
    private val _p1Avatar = MutableStateFlow<String?>(null)
    val p1Avatar = _p1Avatar.asStateFlow()
    
    private val _p2Avatar = MutableStateFlow<String?>(null)
    val p2Avatar = _p2Avatar.asStateFlow()
    
    private val rankedRepository = top.checka.app.data.RankedRepository() // Instantiate properly or via DI

    private fun startRankedSearch() {
        if (currentUserId == null) {
            _errorEvent.value = "Not logged in"
            _isSearching.value = false
            return
        }

        _isSearching.value = true
        isGhostBot = false
        _p2Name.value = "Searching..."
        _p2Avatar.value = null

        viewModelScope.launch {
            // 1. Get current stats for ELO
            // We assume stored in UserPreferences (collected in init)
            val myElo = 1200 // Default or fetch real
            // Ideally we'd have the stats exposed. For now, let's assume 1200 if not set.
            
            // Loop for polling? Or one shot?
            // Simple Logic: Try once, if fail/timeout, go to bot.
            // Retries logic can be added later.
            
            try {
                // Initial Delay to simulate "Searching" UI and prevent instant flicker
                delay(2000) 
                
                val result = rankedRepository.findMatch(currentUserId!!, myElo)
                
                if (result.isSuccess) {
                    val matchData = result.getOrNull()
                    if (matchData != null && matchData.success) {
                         // Match Found!
                         _isSearching.value = false
                         _p2Name.value = matchData.opponent?.name ?: "Unknown"
                         _p2Avatar.value = matchData.opponent?.avatarUrl
                         _opponentData.value = matchData.opponent
                         
                         // Start Game
                         // P2 is opponent.
                         // P1 is us.
                         // Decide who starts? Currently we are P1.
                    } else {
                        // Backend returned success=false? Fallback to local bot?
                        // "No match found"
                         startGhostBot()
                    }
                } else {
                     // Network error
                     startGhostBot()
                }
            } catch (e: Exception) {
                // Error
                startGhostBot()
            }
        }
    }
    
    private fun startGhostBot(failed: Boolean = false) {
        _isSearching.value = false
        isGhostBot = true
        val randomId = (1000..9999).random()
        val botName = "Player $randomId"
        // Random Avatar fallback if needed, but let's use a default or null to show consistent "Bot-ness"
        // Or pick a random Noto URL client side? No, simplest is null or hardcoded.
        // Let's make it look like a real player with a random avatar if possible, or just null.
        val botAvatar = null 
        
        _p2Name.value = botName
        _p2Avatar.value = botAvatar
        
        // Create Fake Opponent Data so profile is clickable
        _opponentData.value = top.checka.app.data.api.OpponentData(
            id = "bot_$randomId",
            name = botName,
            elo = 1200 + (-100..100).random(),
            title = "Beginner",
            avatarUrl = botAvatar,
            level = (1..10).random(),
            winRate = "${(40..60).random()}%",
            totalGames = (50..200).random()
        )
        
        if (_gameState.value.currentPlayer == Player.P2) {
            triggerAITurn()
        }
    }

    fun onMoveSelected(pos: Position) {
        if (_isSearching.value) return 
        if (_isPlaceWallMode.value) return
        if (gameState.value.winner != null) return

        if ((gameMode == GameMode.Solo || isGhostBot) && gameState.value.currentPlayer != Player.P1) return // AI Turn

        // CHECK VALIDITY via pre-calc list (Turn Assist)
        if (pos !in _validMoves.value) {
            _errorEvent.value = "Invalid move"
            return
        }

        val action = GameAction.Move(pos)
        val newState = GameEngine.applyAction(_gameState.value, action)
        
        if (newState != _gameState.value) {
            updateState(newState)
        } else {
            // Should not happen if in validMoves
            _errorEvent.value = "Move failed"
        }
    }

    private fun updateState(newState: GameState) {
        _gameState.value = newState
        checkWinner(newState)
        
        calculateValidMoves(newState) 
        
        if (newState.winner == null && (gameMode == GameMode.Solo || isGhostBot) && newState.currentPlayer == Player.P2) {
            triggerAITurn()
        }
    }

    
    // Feedback
    private val _errorEvent = MutableStateFlow<String?>(null)
    val errorEvent = _errorEvent.asStateFlow()

    fun clearError() {
        _errorEvent.value = null
    }

    fun onIntersectionSelected(row: Int, col: Int) {
        if (_isSearching.value) return
        if (!_isPlaceWallMode.value) return
        if ((gameMode == GameMode.Solo || isGhostBot) && gameState.value.currentPlayer != Player.P1) return

        val current = _previewWall.value
        if (current != null && current.row == row && current.col == col) {
            // Tap same spot -> Rotate
            toggleOrientation()
        } else {
            // New spot -> Preview (Default Horizontal or keep current orientation preference)
            val w = Wall(row, col, _wallOrientation.value)
            val isValid = GameEngine.isValidWallPlacement(w, gameState.value.walls, gameState.value.p1Pos, gameState.value.p2Pos)
            
            if (isValid) {
                _previewWall.value = w
            } else {
                _errorEvent.value = "Invalid wall position (Overlaps or Blocks path)"
            }
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
            val isValid = GameEngine.isValidWallPlacement(newW, gameState.value.walls, gameState.value.p1Pos, gameState.value.p2Pos)
            if (isValid) {
                _previewWall.value = newW
            } else {
                _errorEvent.value = "Cannot rotate: Position invalid"
                _wallOrientation.update { if (it == Orientation.Horizontal) Orientation.Vertical else Orientation.Horizontal }
            }
        }
    }

    fun confirmWall() {
        val w = _previewWall.value 
        if (w == null) {
            _errorEvent.value = "No wall selected"
            return
        }
        val action = GameAction.PlaceWall(w)
        val newState = GameEngine.applyAction(_gameState.value, action)
        if (newState != _gameState.value) {
            updateState(newState)
            _isPlaceWallMode.value = false
            _previewWall.value = null
        } else {
             _errorEvent.value = "Could not place wall"
        }
    }
    
    private fun calculateValidMoves(state: GameState) {
        if (state.winner != null) {
            _validMoves.value = emptyList()
            return
        }
        _validMoves.value = GameEngine.getValidMoves(state)
    }
    
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
            val endTime = System.currentTimeMillis()
            val durationSeconds = (endTime - gameStartTime) / 1000
            
            // Save result to Local DB
            viewModelScope.launch {
                dao.insertMatch(
                    MatchResult(
                        mode = gameMode,
                        difficulty = if (gameMode == GameMode.Solo) difficulty.name else null,
                        player1Name = _p1Name.value,
                        player2Name = _p2Name.value,
                        winnerName = if (state.winner == Player.P1) _p1Name.value else _p2Name.value,
                        totalTurns = state.turnCount,
                        timestamp = endTime
                    )
                )
                
                // Report to Backend
                if (currentUserId != null) {
                    val winnerId = if (state.winner == Player.P1) currentUserId else null
                    
                    authRepository.reportMatch(
                        player1Id = currentUserId!!,
                        player2Id = null,
                        winnerId = winnerId,
                        gameMode = gameMode.name,
                        difficulty = difficulty.name,
                        duration = durationSeconds,
                        turns = state.turnCount
                    )
                }
            }
            // Analytics
            try {
                val winnerName = if (state.winner == Player.P1) _p1Name.value else _p2Name.value
                val result = if (state.winner == Player.P1) "Win" else "Loss"
                val helper = top.checka.app.utils.AnalyticsHelper(getApplication())
                helper.logGameEnd(gameMode.name, result)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
