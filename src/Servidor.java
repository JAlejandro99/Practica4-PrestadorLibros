import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

class Manejador implements Runnable{
    protected Socket cl = null;
    protected int numeroCliente;
    public Manejador(Socket cl) {
        numeroCliente = 0;
        this.cl = cl;
    }

    @Override
    public void run() {}
}

public class Servidor implements Runnable{
    protected int          puerto   = 5000;
    protected ServerSocket s = null;
    protected boolean      detenido    = false;
    protected Thread       runningThread= null;
    protected ExecutorService pool = Executors.newFixedThreadPool(3);
    protected Ventana1 v1;
    protected int numCliente;
    protected Hashtable informarVacio = new Hashtable();
    public ReentrantLock rl,rl2;
    public boolean servidor2;
    PrintWriter pwgeneral;
    BufferedReader brgeneral;
    public ArrayList<String> peticiones;
    
    public Servidor(int puerto, String servidor1, int puerto1){
        if(puerto1!=0){
            esperar(servidor1,puerto1);
        }
        this.puerto = puerto;
        numCliente = 0;
        informarVacio = new Hashtable();
        servidor2 = false;
        rl = new ReentrantLock();
        rl2 = new ReentrantLock();
        v1 = new Ventana1();
        v1.setTitle("Práctica 3 - Repartir Libros, Servidor");
        v1.setVisible(true);
        v1.setResizable(false);
    }
    
    public void esperar(String servidor1, int puerto1){
        try {
            Socket cl = new Socket(servidor1,puerto1);
            System.out.println("Conexion establecida..");
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
            int numeroCliente = Integer.valueOf(br.readLine());
            System.out.println("Numero de cliente: "+numeroCliente);
            pw.println("servidor");
            pw.flush();
            String m1="",m2="";
            while(true){
                System.out.println(br.readLine());
            }//while
        } catch (IOException ex) {}
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        iniciaServidor();
        while(! detenido()){
            Socket cl = null;
            try {
                cl = this.s.accept();
                System.out.println("\nConexion aceptada..");
            } catch (IOException e) {
                if(detenido()) {
                    System.out.println("Servidor detenido.") ;
                    break;
                }throw new RuntimeException("Error al aceptar nueva conexion", e);
            }//catch
            this.pool.execute(new Manejador(cl){
                PrintWriter pw;
                BufferedReader br;
                Boolean salir;
                @Override
                public void run() {
                    try {
                        pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                        br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
                        salir = false;
                        numeroCliente = numCliente;
                        informarVacio.put(numeroCliente,"0");
                        pw.println(String.valueOf(numeroCliente));
                        pw.flush();
                        System.out.println("Cliente conectado: "+String.valueOf(cl.getInetAddress()).substring(1)+":"+cl.getPort()+", id asignada: "+String.valueOf(numCliente));                  
                        numCliente++;
                        Thread enviar = new Thread(){
                            @Override
                            public void run(){
                                while(true){
                                    rl.lock();
                                    if(informarVacio.get(numeroCliente).equals("1")){
                                        informarVacio.remove(numeroCliente);
                                        informarVacio.put(numeroCliente,"0");
                                        pw.println("vacia");
                                        pw.flush();
                                    }
                                    rl.unlock();
                                }
                            }
                        };
                        enviar.start();
                        while(!salir){
                            String linea = br.readLine();
                            if(linea.compareToIgnoreCase("vacio")==0){
                                if(v1.cbd.esLibrosVacio()){
                                    //Informar al cliente que el prestamo de libros ha terminado
                                    pw.println("vacia");
                                    pw.flush();
                                    pw.println("vacia");
                                }else{
                                    pw.println("no");
                                    pw.flush();
                                    pw.println("no");
                                }
                                pw.flush();
                            }else if(linea.compareToIgnoreCase("libro")==0){
                                //auxiliar es la hora
                                String auxiliar = br.readLine();
                                //auxiliar2 es la IP
                                String auxiliar2 = String.valueOf(cl.getInetAddress()).substring(1);
                                String[] resp = v1.cbd.pedirLibro(auxiliar2,auxiliar,"Cliente"+String.valueOf(numeroCliente));
                                System.out.println("\n"+resp[0]);
                                System.out.println(resp[1]);
                                v1.panel.dibujar(resp[1]);
                                System.out.println("Envio la informacion del cliente");
                                pw.println(resp[0]);
                                pw.flush();
                                if(servidor2){
                                    rl2.lock();
                                    peticiones.add(auxiliar+":"+auxiliar2+":"+numCliente+":"+"Cliente"+String.valueOf(numeroCliente)+resp[0]);
                                    rl2.unlock();
                                }
                                v1.refrescarLibros();
                                if(v1.cbd.esLibrosVacio()){
                                    System.out.println("\nLa base de datos está vacia");
                                    //Informar al cliente que el prestamo de libros ha terminado
                                    rl.lock();
                                    Enumeration llaves = informarVacio.keys();
                                    while (llaves.hasMoreElements()) {
                                        int aux = (int) llaves.nextElement();
                                        informarVacio.remove(aux);
                                        informarVacio.put(aux,"1");
                                    }
                                    rl.unlock();
                                }
                            }else if(linea.compareToIgnoreCase("reiniciar")==0){
                                //Agregar la peticion al Servidor2
                            }else if(linea.compareToIgnoreCase("servidor")==0){
                                System.out.println("Servidor 2 Iniciado");
                                enviar.stop();
                                rl.lock();
                                peticiones = new ArrayList<String>();
                                informarVacio.remove(numeroCliente);
                                servidor2 = true;
                                rl.unlock();
                                enviar = new Thread(){
                                    public void run(){
                                        while(true){
                                            try {
                                                pwgeneral = new PrintWriter("auxiliar1.txt");
                                                pwgeneral.close();
                                                if(peticiones.size()>0){
                                                    rl2.lock();for(int i=0;i<peticiones.size();i++){
                                                        pw.println(peticiones.get(i));
                                                        pw.flush();
                                                    }
                                                    peticiones = new ArrayList<String>();
                                                    rl2.unlock();
                                                    new File("auxiliar.txt").delete();
                                                }
                                            } catch (FileNotFoundException ex) {}
                                        }
                                    }
                                };
                                enviar.start();
                            }
                        }//while
                        System.out.println("Solicitud procesada: ");
                    } catch (IOException e) {
                        //report exception somewhere.
                        e.printStackTrace();
                    }
                }
            });
        }//while
        this.pool.shutdown();
        System.out.println("Servidor detenido.") ;
    }


    private synchronized boolean detenido() {
        return this.detenido;
    }

    public synchronized void stop(){
        this.detenido = true;
        try {
            this.s.close();
        } catch (IOException e) {
            throw new RuntimeException("Error al cerrar el socket del servidor", e);
        }
    }

    private void iniciaServidor() {
        try {
            this.s = new ServerSocket(this.puerto);
            System.out.println("Servicio iniciado.. esperando cliente..");
        } catch (IOException e) {
            throw new RuntimeException("No puede iniciar el socket en el puerto: "+s.getLocalPort(), e);
        }
    }
}//class