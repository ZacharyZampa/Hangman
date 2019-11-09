import java.io.*
import java.net.ServerSocket
import java.net.Socket

class Online(private val portNumber: Int = 0, val ip: String = "127.0.0.1") {
    private var os: PrintWriter? = null
    private var `is`: BufferedReader? = null
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null


    /**
     * Host the server
     *
     * @return clientSocket
     */
    fun hostServer(): Socket? {
        try {
            // connect to client
            serverSocket = ServerSocket(portNumber)

            // loop until connected to client
            while (true) {
                try {
                    clientSocket = serverSocket!!.accept()
                    os = PrintWriter(DataOutputStream(clientSocket!!.getOutputStream()))
                    `is` = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))

                    break  // connected so break
                } catch (ex: IOException) {
                    System.err.println("Error accepting client connection: ${ex.message}")
                }

            }
        } catch (ex: IOException) {
            System.err.println("Error opening server:  ${ex.message}")
        }

        return clientSocket
    }

    /**
     * Process client response and return the response to the user
     *
     * @return int
     * @throws Exception
     */
    @Throws(Exception::class)
    fun processLANResponse(): String {
        `is` = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))

        // read data in from the client
        return `is`!!.readLine()
    }

    /**
     * Send the user response to the other client
     *
     * @param move
     * @throws Exception
     */
    @Throws(Exception::class)
    fun sendLANResponse(move: String) {
        os?.println(move)
        os?.flush()
    }


    fun joinServer() {
        // attempt connection
        var result = false
        var retry = 5
        while (retry > 0 && !result) {
            try {
                connect()
                result = true
            } catch (err: IOException) {
                System.err.println("Error during protocol $err")
            }

            retry--
        }
    }

    /**
     * Establish the connection with the server
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun connect() {
        clientSocket = Socket(ip, portNumber)
        `is` = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))
        os = PrintWriter(DataOutputStream(clientSocket!!.getOutputStream()))
    }

}