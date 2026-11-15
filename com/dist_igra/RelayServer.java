package com.dist_igra;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Središnji poslužitelj koji prosljeđuje poruke između procesa.
 * Svaki proces otvara SAMO JEDNU odlaznu vezu prema ovom poslužitelju,
 * pa nijedan proces ne mora biti dohvatljiv izvana (nema problema s NAT-om).
 *
 * Protokol:
 *   klijent -> "hello <basename> <id>"
 *   server  -> "ok"  (ili "error <razlog>")
 *   zatim obje strane razmjenjuju retke oblika "src dest tag poruka#"
 *   Server čita samo drugi broj (dest) i redak šalje nepromijenjen dalje.
 */
public class RelayServer {

    /** Jedna "soba" = jedna aplikacija (jedno bazno ime). */
    static class Room {
        final Map<Integer, PrintWriter> out = new HashMap<>();
        // poruke za procese koji se još nisu javili
        final Map<Integer, List<String>> pending = new HashMap<>();
    }

    static final Map<String, Room> rooms = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(Symbols.ServerPort);
        System.out.println("RelayServer started on port " + Symbols.ServerPort);
        while (true) {
            Socket s = listener.accept();
            new Thread(() -> handle(s)).start();
        }
    }

    static void handle(Socket s) {
        Room room = null;
        String base = null;
        int id = -1;
        PrintWriter out = null;
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream());

            String first = in.readLine();
            if (first == null) return;
            StringTokenizer st = new StringTokenizer(first);
            if (!st.nextToken().equals("hello")) return;
            base = st.nextToken();
            id = Integer.parseInt(st.nextToken());

            // zaključavanje uvijek ide redom: rooms -> room
            synchronized (rooms) {
                room = rooms.computeIfAbsent(base, k -> new Room());
                synchronized (room) {
                    if (room.out.containsKey(id)) {
                        out.println("error id " + id + " je vec spojen");
                        out.flush();
                        room = null;        // nemoj čistiti tuđi unos u finally
                        return;
                    }
                    room.out.put(id, out);
                    out.println("ok");
                    // isporuči poruke koje su stigle prije nego što se proces javio
                    List<String> waiting = room.pending.remove(id);
                    if (waiting != null)
                        for (String line : waiting) out.println(line);
                    out.flush();
                }
            }
            System.out.println("Spojen proces " + id + " (" + base + ") s "
                    + s.getRemoteSocketAddress());

            String line;
            while ((line = in.readLine()) != null) {
                StringTokenizer lt = new StringTokenizer(line);
                lt.nextToken();                          // src
                int dest = Integer.parseInt(lt.nextToken());
                synchronized (room) {
                    PrintWriter o = room.out.get(dest);
                    if (o != null) {
                        o.println(line);
                        o.flush();
                    } else {
                        room.pending
                            .computeIfAbsent(dest, k -> new ArrayList<>())
                            .add(line);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Greska: " + e);
        } finally {
            if (room != null) {
                synchronized (rooms) {
                    synchronized (room) {
                        if (room.out.get(id) == out) room.out.remove(id);
                        // kad se svi odspoje, soba se briše (nema starih poruka za iduće pokretanje)
                        if (room.out.isEmpty()) rooms.remove(base);
                    }
                }
                System.out.println("Odspojen proces " + id + " (" + base + ")");
            }
            try { s.close(); } catch (IOException e) { }
        }
    }
}
