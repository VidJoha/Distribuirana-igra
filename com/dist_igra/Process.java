package com.dist_igra;

/**
 *
 * @author Vid
 */
import java.io.*;
import java.util.Scanner;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;

public class Process implements MsgHandler {
    public static final String RESET = "\u001B[0m";
    public static final String RED    = "\u001B[31m";
    public static final String GREEN  = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE   = "\u001B[34m";
    public static final String[] Boje = {RED,GREEN,YELLOW,BLUE,RESET};
    public static final char[] BojeInicijali = {'R','G','Y','B','N'};

    public record IntPair(int first, int second) {}
    int N, myId;
    Linker comm;
    String MojaBoja;
    char MojInicijal;
    int[][] PlocaBrojevi;
    List<IntPair> Trenutna_Bitna_Polja_Brojevi;
    List<IntPair> Buduca_Bitna_Polja_Brojevi;
    char[][] PlocaBoje;
    public int indexOf(char c){
        int index=-1;
        for (int i = 0; i < 5; i++) {
            if (BojeInicijali[i] == c) {
                index = i; 
                return index;
            }
        }
        return index;
    }
    public void isprintajPlocu(){
        for (int i=0;i<10;i++) {
            for (int j=0;j<10;j++) {
                System.out.print(Boje[indexOf(PlocaBoje[i][j])]+PlocaBrojevi[i][j] + " "+RESET);
            }
            System.out.println();
        }
    }
    public Process(Linker initComm) {
        comm = initComm;
        myId = comm.getMyId();
        N = comm.getNumProc();
        MojaBoja=Boje[myId];
        MojInicijal=BojeInicijali[myId];
        PlocaBrojevi = new int[10][10];
        PlocaBrojevi[1][1]=3;
        PlocaBrojevi[1][0]=4;
        PlocaBrojevi[1][2]=4;
        PlocaBoje = new char[10][10];
        
        for (char[] row : PlocaBoje) {
            Arrays.fill(row, 'N');
        }
        PlocaBoje[1][1]='R';
        PlocaBoje[1][0]='R';
        PlocaBoje[1][2]='G';
        Trenutna_Bitna_Polja_Brojevi = new ArrayList<>();
        Buduca_Bitna_Polja_Brojevi = new ArrayList<>();
        isprintajPlocu();
        }

    /*public String matrixToString(){
        String String_Matrica = new String("");
        
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                String_Matrica += PlocaBrojevi[i][j];
            }
        }

        return String_Matrica;
    }
    public void matrixFromString(Msg m){
        String String_Matrica = m.getMessage();
        String[] Brojevi = String_Matrica.trim().split("\\s+");
        PlocaBrojevi= new int[10][10];
        for(int i=0;i<10;i++){
            for(int j=0;j<10;j++){
                PlocaBrojevi[i][j]= Integer.parseInt(Brojevi[10*i+j]);
            }
        }
    }*/
    void addUnique(List<IntPair> list, int i,int j) {
        IntPair k = new IntPair(i, j);
    if (!list.contains(k)) {
        list.add(k);
        }
    }
    public void dodaj(int i,int j,int gore,int desno, int dolje, int lijevo,int src){
        if(gore!=0){
            PlocaBrojevi[i-1][j]+=gore;
            PlocaBoje[i-1][j]=BojeInicijali[src];
            if(PlocaBrojevi[i-1][j]>=5){
                addUnique(Buduca_Bitna_Polja_Brojevi, i-1, j);
            }
        }
        if(desno!=0){
            PlocaBrojevi[i][j+1]+=desno;
            PlocaBoje[i][j+1]=BojeInicijali[src];
            if(PlocaBrojevi[i][j+1]>=5){
                addUnique(Buduca_Bitna_Polja_Brojevi, i, j+1);
            }
        }
        if(dolje!=0){
            PlocaBrojevi[i+1][j]+=dolje;
            PlocaBoje[i+1][j]=BojeInicijali[src];
            if(PlocaBrojevi[i+1][j]>=5){
                addUnique(Buduca_Bitna_Polja_Brojevi, i+1, j);
            }
        }
        if(lijevo!=0){
            PlocaBrojevi[i][j-1]+=lijevo;
            PlocaBoje[i][j-1]=BojeInicijali[src];
            if(PlocaBrojevi[i][j-1]>=5){
                addUnique(Buduca_Bitna_Polja_Brojevi, i, j-1);
            }
        }
    }
    public void raspukni(int i,int j,int src){
        if(PlocaBrojevi[i][j]>=5){
            if(i==0){
                if(j==0){
                    dodaj(i,j,0,1,1,0,src);
                }
                else if(j==9){
                    dodaj(i,j,0,0,1,1,src);
                }
                else{
                    dodaj(i,j,0,1,1,1,src);
                }
            }
            else if(i==9){
                if(j==0){
                    dodaj(i,j,1,1,0,0,src);
                }
                else if(j==9){
                    dodaj(i,j,1,0,0,1,src);
                }
                else{
                    dodaj(i,j,1,1,0,1,src);
                }
            }
            else{
                if(j==0){
                    dodaj(i,j,1,1,1,0,src);
                }
                else if(j==9){
                    dodaj(i,j,1,0,1,1,src);
                }
                else{
                    dodaj(i,j,1,1,1,1,src);
                }
            }
            PlocaBrojevi[i][j]=1;
        }
    }  
    public void simuliraj(int i,int j,int src){
        System.out.println("Simuliram potez");
        PlocaBrojevi[i][j] += 1;
        if(PlocaBrojevi[i][j]==1){
            PlocaBoje[i][j]=BojeInicijali[src];
        }
        if(PlocaBrojevi[i][j]==5){
            raspukni(i,j,src);
        }
        isprintajPlocu();

        while(!Buduca_Bitna_Polja_Brojevi.isEmpty()){
            System.out.println("Dodatna simulacija jer postoji lančana reakcija");
            Trenutna_Bitna_Polja_Brojevi = new ArrayList<IntPair>(Buduca_Bitna_Polja_Brojevi);
            Buduca_Bitna_Polja_Brojevi.clear();

            for(int k=0;k<Trenutna_Bitna_Polja_Brojevi.size();k++){
                System.out.println(Trenutna_Bitna_Polja_Brojevi.get(k).first());
                System.out.println(Trenutna_Bitna_Polja_Brojevi.get(k).second());
                raspukni(Trenutna_Bitna_Polja_Brojevi.get(k).first(),Trenutna_Bitna_Polja_Brojevi.get(k).second(),src);
            }
            isprintajPlocu();
        }
        
    }
    public synchronized void handleMsg(Msg m, int src, String tag) {

        if(tag.equals("potez")){
            String Msg_String = m.getMessage();
            String[] RedStupac =Msg_String.trim().split("\\s+");
            int red = Integer.parseInt(RedStupac[0]);
            int stupac = Integer.parseInt(RedStupac[1]);
            simuliraj(red,stupac,src);
            
        }
        else if(tag.equals("tvoj_red")){
            int red=-1;
            int stupac=-1;
            boolean valid=false;
            while(true){
                System.out.println("Ti si na potezu, upiši red i stupac gdje želiš postaviti točku");
                Scanner scanner = new Scanner(System.in);
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

                if(PlocaBoje[red][stupac]==MojInicijal || PlocaBoje[red][stupac]=='N'){
                    break;
                }
                else{
                    System.out.println("Ne možeš postaviti točku na to mjesto, upiši neko drugo mjesto");
                }
            }
            simuliraj(red,stupac,myId);
            String Msg_String = red + " " + stupac;
            broadcastMsg("potez", Msg_String);
            sendMsg((myId +1)%N,"tvoj_red");
        }
    }
    public void sendMsg(int destId, String tag, String msg) {
        //Util.println("Sending msg to " + destId + ":" +tag + " " + msg);
        comm.sendMsg(destId, tag, msg);
        }
    public void sendMsg(int destId, String tag, int msg) {
        sendMsg(destId, tag, String.valueOf(msg)+" ");
        }
    public void sendMsg(int destId, String tag, int msg1, int msg2) {
        sendMsg(destId,tag,String.valueOf(msg1)
        +" "+String.valueOf(msg2)+" ");
        }
    public void sendMsg(int destId, String tag) {
        sendMsg(destId, tag, " 0 ");
        }

    public void sendMsg(int desId, String tag,int runda, Vector<Integer> delta){
        if(tag.equals("Alive")){
            System.out.println("Javljam procesu "+desId+" da sam živ");
        }
        else if(tag.equals("Consensus")){
            System.out.println("Šaljem vector "+delta.toString()+" procesu "+desId+" u rundi "+runda);
        }

        String MsgString=String.valueOf(runda);
        for(int i=0;i<delta.size();i++){
            MsgString=MsgString+" "+delta.get(i);
        }
        sendMsg(desId,tag,MsgString);
        }
    public void broadcastMsg(String tag, String msg) {
        for (int i = 0; i < N; i++)
        if (i != myId) sendMsg(i, tag, msg);
        }
    public void sendToNeighbors(String tag, int msg) {
        for (int i = 0; i < N; i++)
        if (isNeighbor(i)) sendMsg(i, tag, msg);
        }
    public boolean isNeighbor(int i) {
        if (comm.neighbors.contains(i)) return true;
        else return false;
        }
    public Msg receiveMsg(int fromId) {
        try {
            return comm.receiveMsg(fromId);
        } catch (IOException e){
        System.out.println(e);
        comm.close();
        return null;
        }
    }
    public synchronized void myWait() {
        try {
            wait();
        } catch (InterruptedException e) {System.err.println(e);
        }
    }
}
