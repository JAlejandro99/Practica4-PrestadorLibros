import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

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
    protected ArrayList<Socket> cls;
    protected Hashtable informarVacio = new Hashtable();
    public ReentrantLock rl;
    
    public Servidor(int puerto){
        this.puerto = puerto;
        numCliente = 0;
        informarVacio = new Hashtable();
        rl = new ReentrantLock();
        v1 = new Ventana1();
        v1.setTitle("Práctica 3 - Repartir Libros, Servidor");
        v1.setVisible(true);
        v1.setResizable(false);
        cls = new ArrayList<Socket>();
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
                cls.add(cl);
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
                                String[] resp = v1.cbd.pedirLibro(String.valueOf(cl.getInetAddress()).substring(1),br.readLine(),"Cliente"+String.valueOf(numeroCliente));
                                System.out.println("\n"+resp[0]);
                                System.out.println(resp[1]);
                                v1.panel.dibujar(resp[1]);
                                System.out.println("Envio la informacion del cliente");
                                pw.println(resp[0]);
                                pw.flush();
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

    public static void main(String[] args){
        Servidor server = new Servidor(5000);
        new Thread(server).start();
    }//main
}//class