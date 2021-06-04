import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Cliente {
    static int PUERTO;
    static int PUERTO2;
    static String servidor;
    static String servidor2;
    static int numeroCliente;
    static Ventana2 v2;
    static PrintWriter pw;
    static BufferedReader br1;
    
    public static void main(String[] args){
        numeroCliente = 0;
        pedirServidoryPuerto();
    }//main
    public static void pedirServidoryPuerto(){
        ServidoryPuerto sp = new ServidoryPuerto();
        sp.setTitle("Práctica 3 - Repartir Libros, Servidor y Puerto");
        sp.setVisible(true);
        sp.setResizable(false);
        sp.aceptar.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                PUERTO = Integer.valueOf(sp.puerto.getText());
                servidor = sp.IP.getText();
                sp.setVisible(false);
                v2 = new Ventana2();
                v2.setTitle("Práctica 3 - Repartir Libros, Cliente");
                v2.setVisible(true);
                v2.setResizable(false);
                Thread cliente = new Thread(){
                    boolean seguro;
                    Thread recibir;
                    public void arrancarHilo(){
                        recibir = new Thread(){
                            String mensaje;
                            @Override
                            public void run(){
                                try {
                                    mensaje = br1.readLine();
                                    if(mensaje.equals("vacia")){
                                        int resp = JOptionPane.showConfirmDialog(null, "El sistema ha prestado todos los libros, ¿deseas seguir en el sistema?");
                                        if(resp==1){
                                            pw.println("reiniciar");
                                            pw.flush();
                                            System.out.println("Regresando libros");
                                            v2.infoLibros.setText("");
                                            System.exit(0);
                                        }
                                    }
                                } catch (IOException ex) {
                                    System.out.println("Conexión con el Servidor perdida 1");
                                }
                            }
                        };
                        recibir.start();
                    }
                    @Override
                    public void run(){
                        v2.pedirLibro.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e){
                                recibir.stop();
                                try {
                                    pw.println("vacio");
                                    pw.flush();
                                    if(br1.readLine().equals("no")){
                                        pw.println("libro");
                                        pw.flush();
                                        pw.println(v2.r1.getHora2());
                                        pw.flush();
                                        String mensaje = br1.readLine();
                                        if(mensaje.length()<10)
                                            mensaje = br1.readLine();
                                        System.out.println(mensaje);
                                        String[] respuesta2 = new String[4];
                                        int aux=6;
                                        int k=0;
                                        for(int i=0;i<mensaje.length();i++){
                                            if(mensaje.charAt(i)==','){
                                                //System.out.println(mensaje.substring(aux,i));
                                                respuesta2[k] = mensaje.substring(aux,i);
                                                aux=i+1;
                                                k+=1;
                                            }
                                        }
                                        v2.infoLibros.append(respuesta2[0]+"\n");
                                        v2.infoLibros.append(respuesta2[1]+"\n");
                                        v2.infoLibros.append(respuesta2[2]+"\n");
                                        v2.infoLibros.append(respuesta2[3]+"\n\n");
                                    }else{
                                        JOptionPane.showMessageDialog( null, "El prestador de libros ha agotado todos los libros de su inventario, no se ha podido solicitar un libro." , "Libros agotados" , JOptionPane.INFORMATION_MESSAGE );
                                    }
                                } catch (IOException ex) {
                                    System.out.println("Conexión con el Servidor perdida 2");
                                }
                                arrancarHilo();
                            }
                        });
                        v2.reiniciar.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e){
                                pw.println("reiniciar");
                                pw.flush();
                            }
                        });
                        v2.salir.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e){
                                pw.println("reiniciar");
                                pw.flush();
                                System.exit(0);
                            }
                        });
                        try{
                            Socket cl = new Socket(servidor,PUERTO);
                            System.out.println("Conexion establecida..");
                            pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                            br1 = new BufferedReader(new InputStreamReader(cl.getInputStream()));
                            numeroCliente = Integer.valueOf(br1.readLine());
                            System.out.println("Numero de cliente: "+numeroCliente);
                            arrancarHilo();
                            while(true){}//while
                        }catch(IOException e){
                            System.out.println("Conexión con el Servidor perdida 3");
                        }//catch
                    }
                };
                cliente.start();
            }
        });
    }
}