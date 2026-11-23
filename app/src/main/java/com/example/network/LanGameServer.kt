package com.example.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets

interface LanRequestHandler {
    fun getPublicRoomInfo(): JSONObject
    fun handleJoin(body: JSONObject): ApiResponse
    fun handleGetState(bearerToken: String): ApiResponse
    fun handleAction(bearerToken: String, body: JSONObject): ApiResponse
    fun handleLeave(bearerToken: String): ApiResponse
}

data class ApiResponse(
    val statusCode: Int = 200,
    val payload: JSONObject
)

class LanGameServer(
    private val handler: LanRequestHandler
) {
    private var serverSocket: ServerSocket? = null
    private var scope: CoroutineScope? = null
    private var acceptJob: Job? = null

    @Volatile
    var boundPort: Int = 0
        private set

    val isRunning: Boolean
        get() = serverSocket?.isClosed == false && boundPort > 0

    fun start(preferredPort: Int = 8088): Int {
        if (isRunning) return boundPort
        stop()

        val socket = bindAvailableSocket(preferredPort)
            ?: throw IllegalStateException("无法绑定局域网端口，请重试")
        serverSocket = socket
        boundPort = socket.localPort

        val serverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope = serverScope
        acceptJob = serverScope.launch {
            while (isActive && !socket.isClosed) {
                try {
                    val client = socket.accept()
                    launch { handleClient(client) }
                } catch (_: Exception) {
                    if (socket.isClosed) break
                }
            }
        }
        return boundPort
    }

    fun stop() {
        boundPort = 0
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }
        serverSocket = null
        acceptJob?.cancel()
        acceptJob = null
        scope?.cancel()
        scope = null
    }

    private fun bindAvailableSocket(preferredPort: Int): ServerSocket? {
        for (port in preferredPort..(preferredPort + 15)) {
            try {
                return ServerSocket().apply {
                    reuseAddress = true
                    bind(InetSocketAddress("0.0.0.0", port))
                }
            } catch (_: Exception) {
                // Try next port
            }
        }
        return try {
            ServerSocket().apply {
                reuseAddress = true
                bind(InetSocketAddress("0.0.0.0", 0))
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun handleClient(socket: Socket) {
        socket.use { client ->
            try {
                client.soTimeout = 5000
                val input = BufferedInputStream(client.getInputStream())
                val output = client.getOutputStream()

                val request = parseHttpRequest(input) ?: return
                val path = request.path.substringBefore("?")

                when {
                    request.method == "GET" && (path == "/" || path == "/index.html") -> {
                        writeResponse(
                            output = output,
                            statusCode = 200,
                            contentType = "text/html; charset=utf-8",
                            bodyBytes = LanWebAssets.CLIENT_HTML.toByteArray(StandardCharsets.UTF_8)
                        )
                    }
                    request.method == "GET" && path == "/api/info" -> {
                        writeJson(output, 200, handler.getPublicRoomInfo())
                    }
                    request.method == "POST" && path == "/api/join" -> {
                        val json = parseJsonBody(request.body)
                        if (json == null) {
                            writeJson(output, 400, errorJson("请求格式无效"))
                        } else {
                            val res = handler.handleJoin(json)
                            writeJson(output, res.statusCode, res.payload)
                        }
                    }
                    request.method == "GET" && path == "/api/state" -> {
                        val token = extractBearerToken(request.headers)
                        if (token == null) {
                            writeJson(output, 401, errorJson("未授权访问，请重新加入房间"))
                        } else {
                            val res = handler.handleGetState(token)
                            writeJson(output, res.statusCode, res.payload)
                        }
                    }
                    request.method == "POST" && path == "/api/action" -> {
                        val token = extractBearerToken(request.headers)
                        val json = parseJsonBody(request.body)
                        when {
                            token == null -> writeJson(output, 401, errorJson("未授权访问，请重新加入房间"))
                            json == null -> writeJson(output, 400, errorJson("指令格式无效"))
                            else -> {
                                val res = handler.handleAction(token, json)
                                writeJson(output, res.statusCode, res.payload)
                            }
                        }
                    }
                    request.method == "POST" && path == "/api/leave" -> {
                        val token = extractBearerToken(request.headers)
                        if (token == null) {
                            writeJson(output, 401, errorJson("未授权访问"))
                        } else {
                            val res = handler.handleLeave(token)
                            writeJson(output, res.statusCode, res.payload)
                        }
                    }
                    else -> {
                        writeJson(output, 404, errorJson("接口不存在"))
                    }
                }
            } catch (_: Exception) {
                // Socket closed or malformed request; ignore gracefully
            }
        }
    }

    private data class ParsedHttpRequest(
        val method: String,
        val path: String,
        val headers: Map<String, String>,
        val body: String
    )

    private fun parseHttpRequest(input: BufferedInputStream): ParsedHttpRequest? {
        val headerBuffer = ByteArrayOutputStream()
        var state = 0
        var totalHeaderBytes = 0
        while (totalHeaderBytes < MAX_HEADER_BYTES) {
            val b = input.read()
            if (b == -1) return null
            headerBuffer.write(b)
            totalHeaderBytes++
            state = when {
                state == 0 && b == '\r'.code -> 1
                state == 1 && b == '\n'.code -> 2
                state == 2 && b == '\r'.code -> 3
                state == 3 && b == '\n'.code -> 4
                b == '\r'.code -> 1
                else -> 0
            }
            if (state == 4) break
        }
        if (state != 4) return null

        val headerText = headerBuffer.toString(StandardCharsets.UTF_8.name())
        val lines = headerText.split("\r\n").filter { it.isNotEmpty() }
        if (lines.isEmpty()) return null

        val requestParts = lines.first().split(" ")
        if (requestParts.size < 2) return null
        val method = requestParts[0].uppercase()
        val path = requestParts[1]

        val headers = mutableMapOf<String, String>()
        for (i in 1 until lines.size) {
            val colon = lines[i].indexOf(':')
            if (colon > 0) {
                val k = lines[i].substring(0, colon).trim().lowercase()
                val v = lines[i].substring(colon + 1).trim()
                headers[k] = v
            }
        }

        val contentLength = headers["content-length"]?.toIntOrNull()?.coerceIn(0, MAX_BODY_BYTES) ?: 0
        val bodyBytes = ByteArray(contentLength)
        var readSoFar = 0
        while (readSoFar < contentLength) {
            val n = input.read(bodyBytes, readSoFar, contentLength - readSoFar)
            if (n == -1) break
            readSoFar += n
        }
        val body = String(bodyBytes, 0, readSoFar, StandardCharsets.UTF_8)
        return ParsedHttpRequest(method, path, headers, body)
    }

    private fun extractBearerToken(headers: Map<String, String>): String? {
        val auth = headers["authorization"] ?: return null
        if (!auth.startsWith("Bearer ", ignoreCase = true)) return null
        val token = auth.substring(7).trim()
        return token.takeIf { it.length in 16..128 }
    }

    private fun parseJsonBody(raw: String): JSONObject? {
        if (raw.isBlank()) return JSONObject()
        return try {
            JSONObject(raw)
        } catch (_: Exception) {
            null
        }
    }

    private fun writeJson(output: OutputStream, statusCode: Int, json: JSONObject) {
        writeResponse(
            output = output,
            statusCode = statusCode,
            contentType = "application/json; charset=utf-8",
            bodyBytes = json.toString().toByteArray(StandardCharsets.UTF_8)
        )
    }

    private fun writeResponse(
        output: OutputStream,
        statusCode: Int,
        contentType: String,
        bodyBytes: ByteArray
    ) {
        val reason = when (statusCode) {
            200 -> "OK"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not Found"
            409 -> "Conflict"
            else -> "Internal Server Error"
        }
        val header = buildString {
            append("HTTP/1.1 $statusCode $reason\r\n")
            append("Content-Type: $contentType\r\n")
            append("Content-Length: ${bodyBytes.size}\r\n")
            append("Cache-Control: no-store, max-age=0\r\n")
            append("X-Content-Type-Options: nosniff\r\n")
            append("Connection: close\r\n")
            append("\r\n")
        }
        output.write(header.toByteArray(StandardCharsets.UTF_8))
        output.write(bodyBytes)
        output.flush()
    }

    companion object {
        private const val MAX_HEADER_BYTES = 8 * 1024
        private const val MAX_BODY_BYTES = 16 * 1024

        fun errorJson(message: String): JSONObject = JSONObject().apply {
            put("ok", false)
            put("error", message)
        }
    }
}
