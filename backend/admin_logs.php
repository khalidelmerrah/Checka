<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checka Logs</title>
    <style>
        body {
            background-color: #1e1e1e;
            color: #d4d4d4;
            font-family: monospace;
            padding: 20px;
        }

        #log-container {
            white-space: pre-wrap;
            word-wrap: break-word;
        }

        .log-entry {
            margin-bottom: 2px;
        }

        .status-bar {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            background: #333;
            padding: 10px;
            border-bottom: 1px solid #555;
            display: flex;
            justify-content: space-between;
        }

        .content {
            margin-top: 50px;
        }
    </style>
</head>

<body>
    <div class="status-bar">
        <span>Checka Backend Logs</span>
        <button onclick="fetchLogs()">Refresh Now</button>
    </div>
    <div class="content" id="log-container">Loading...</div>

    <script>
        const logContainer = document.getElementById('log-container');

        async function fetchLogs() {
            try {
                const response = await fetch('api/get_logs.php');
                const text = await response.text();
                logContainer.innerText = text;
                window.scrollTo(0, document.body.scrollHeight);
            } catch (e) {
                logContainer.innerText = "Error fetching logs: " + e;
            }
        }

        // Poll every 2 seconds
        setInterval(fetchLogs, 2000);
        fetchLogs();
    </script>
</body>

</html>