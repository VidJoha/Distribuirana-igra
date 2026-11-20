package com.dist_igra;
import java.util.InputMismatchException;
import java.util.Scanner;

import com.dist_igra.MsgHandler;

public class ProcessTester {

    public static void main(String[] args) throws Exception {
    Linker comm = null;
    try {
        String baseName = args[0];
        int myId = Integer.parseInt(args[1]);
        int numProc = Integer.parseInt(args[2]);
        comm = new Linker(baseName, myId, numProc);
        Process igrac = new Process(comm);
        boolean PrvaRunda = true;
        for (int i = 0; i < numProc; i++)
            if (i != myId)
                (new ListenerThread(i, (MsgHandler)igrac)).start();

        while (true) {
            if (myId == 0 && PrvaRunda) {
                System.out.println("Prvi si na potezu, upiši red i stupac gdje želiš postaviti točku");
                Scanner scanner = new Scanner(System.in);
                int red = 0;
                int stupac = 0;
                boolean valid = false;
                while(!valid){
                    try {
                        red = scanner.nextInt();
                        stupac = scanner.nextInt();
                        valid=true;
                    } catch (InputMismatchException e) {
                        if (scanner.hasNext()) {
                            scanner.next(); // Discard the invalid token
                        }
                        System.out.println("Nisi unio 2 cijela broja");
                    }
                }
                System.out.println("Tvoj potez je "+red+" "+stupac);
                igrac.simuliraj(red,stupac,myId);
                String Msg_String = red + " " + stupac;
                igrac.broadcastMsg("potez", Msg_String);
                igrac.sendMsg((myId+1)%numProc,"tvoj_red");
                PrvaRunda=false;
                continue;
            }
            }
    }
    catch (InterruptedException e) {
        if (comm != null) comm.close();
        }
    catch (Exception e) {
        System.out.println(e);
        e.printStackTrace();
        }
    }
}