package com.dist_igra;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Connector koji umjesto N-1 izravnih veza otvara JEDNU odlaznu vezu
 * prema RelayServeru (adresa u Symbols.nameServer, port Symbols.ServerPort).
 * Linker se ne mijenja: i dalje dobiva dataIn[i] i dataOut[i] po procesu.
 */
public class Connector {
    Socket socket;

    /** "BufferedReader" koji redke ne čita iz mreže nego iz reda (queue). */
    static class QueueReader extends BufferedReader {
        static final String EOF = new String("EOF");   // oznaka kraja veze (usporedba s ==)
        final BlockingQueue<String> q = new LinkedBlockingQueue<>();

        QueueReader() { super(new StringReader("")); }

        void put(String line) { q.add(line); }

        @Override
        public String readLine() throws IOException {
            try {
                String s = q.take();
                if (s == EOF) { q.add(EOF); return null; }   // veza je pala
                return s;
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    public void Connect(String basename, int myId, int numProc,
                        BufferedReader[] dataIn, PrintWriter[] dataOut) throws Exception {
        socket = new Socket(Symbols.nameServer, Symbols.ServerPort);
        final BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        final PrintWriter out = new PrintWriter(socket.getOutputStream());

        /* prijava na relay */
        out.println("hello " + basename + " " + myId);
        out.flush();
        String reply = in.readLine();
        if (reply == null || !reply.equals("ok"))
            throw new Exception("Relay je odbio vezu: " + reply);
        Util.println("Spojen na relay " + Symbols.nameServer + ":" + Symbols.ServerPort);

        /* za svakog pošiljatelja jedan red; svi izlazi dijele isti PrintWriter */
        final QueueReader[] queues = new QueueReader[numProc];
        for (int i = 0; i < numProc; i++) {
            queues[i] = new QueueReader();
            dataIn[i] = queues[i];
            dataOut[i] = out;
        }

        /* dretva koja čita s mreže i razvrstava poruke po pošiljatelju (prvi broj u retku) */
        Thread reader = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    int src = Integer.parseInt(line.substring(0, line.indexOf(' ')));
                    if (src >= 0 && src < numProc) queues[src].put(line);
                }
            } catch (Exception e) {
                // veza je pala ili je zatvorena
            } finally {
                for (QueueReader qr : queues) qr.put(QueueReader.EOF);
            }
        });
        reader.setDaemon(true);
        reader.start();
    }

    public void closeSockets() {
        try {
            socket.close();
        } catch (Exception e) { System.err.println(e); }
    }
}
