import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.lang.Thread.sleep;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class RelojGrafico extends JPanel implements Runnable {
    public JButton boton;
    public JButton boton2;
    public int horas;
    public int minutos;
    public int segundos;
    public float velocidad;
    public boolean aleatorio,continuar;
    public Thread reloj;
    public ReajustarHora rh;
    
    public RelojGrafico(boolean aleatorio, int x, int y){
        horas = 0;
        minutos = 0;
        segundos = 0;
        velocidad = 1;//1
        this.setBounds(x,y,250,100);
        this.setBackground(new Color(38,70,95));
        this.inicializarBoton();
        this.aleatorio = aleatorio;
        continuar = true;
    }
    public Integer[] getHora(){
        Integer[] ret = new Integer[3];
        ret[0] = horas;
        ret[1] = minutos;
        ret[2] = segundos;
        return ret;
    }
    public String getHora2(){
        String hora;
        if(horas<10)
            hora = "0"+horas+":";
        else
            hora = horas+":";
        if(minutos<10)
            hora += "0"+minutos+":";
        else
            hora += minutos+":";
        if(segundos<10)
            hora += "0"+segundos;
        else
            hora += segundos;
        return hora;
    }
    public void iniciarReloj(){
        reloj = new Thread(){
            @Override
            public void run(){
                while(continuar){
                    try {
                        sleep((int) (1000/velocidad));
                    } catch (InterruptedException ex) {}
                    incrementar1segundo();
                    repaint();
                }
            }
        };
        reloj.start();
    }
    public void incrementar1segundo(){
        if(segundos>58){
            segundos = 0;
            incrementar1minuto();
        }else
            segundos+=1;
    }
    public void incrementar1minuto(){
        if(minutos>58){
            minutos = 0;
            incrementar1hora();
        }else
            minutos+=1;
    }
    public void incrementar1hora(){
        if(horas>22)
            horas = 0;
        else
            horas+=1;
    }
    protected void botonDigitado(){
        //Detener reloj, modificar hora y seguir ejecutando el reloj
        continuar = false;
        rh = new ReajustarHora();
        rh.setTitle("Reajustar hora");
        rh.setVisible(true);      
        rh.aceptar.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                horas = rh.getHoras();
                minutos = rh.getMinutos();
                segundos = rh.getSegundos();
                velocidad = rh.getVelocidad();
                repaint();
                continuar = true;
                iniciarReloj();
            }
        });
    }
    public void reasignarHora(int horas, int minutos, int segundos){
        this.horas = horas;
        this.minutos = minutos;
        this.segundos = segundos;
        this.velocidad = 1;
        repaint();
    }
    protected void inicializarBoton(){
        boton = new JButton();
        ImageIcon icono=new ImageIcon((new ImageIcon("src/icons/editar.png").getImage()).getScaledInstance(20,20,Image.SCALE_SMOOTH));
        boton.setIcon(icono);
        boton.setSize(10,10);
        boton.setOpaque(false);
        boton.setBackground(new Color(0,0,0,0));
        boton.setBorderPainted(false);
        this.setLayout(null);
        boton.setBounds(225,5,20,20);
        boton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                botonDigitado();
            }
        });
        this.add(boton);
        boton2 = new JButton();
        ImageIcon icono2=new ImageIcon((new ImageIcon("src/icons/enviado2.png").getImage()).getScaledInstance(26,26,Image.SCALE_SMOOTH));
        boton2.setIcon(icono2);
        boton2.setSize(37,37);
        boton2.setOpaque(false);
        boton2.setBackground(new Color(0,0,0,0));
        boton2.setBorderPainted(false);
        this.setLayout(null);
        boton2.setBounds(220,35,30,30);
        this.add(boton2);
    }
    @Override
    public void paint(Graphics g){
        super.paint(g);
        g.setColor(Color.white);
        g.fillRoundRect(0,0,this.getWidth()-27,this.getHeight()-2,15,15);
        g.setColor(Color.black);
        String hora = getHora2();
        g.setFont( new Font( "Tahoma", Font.BOLD, 45 ) );
        g.drawString(hora,9,70);
        if(velocidad!=1){
            g.setFont( new Font( "Tahoma", Font.ITALIC, 20 ) );
            g.drawString("x"+velocidad,11,93);
        }
    }
    @Override
    public void run() {
        if(aleatorio){
            horaAleatoria();
        }else{
            Calendar c = new GregorianCalendar();
            Date fechaHoraActual = new Date();
            c.setTime(fechaHoraActual);
            horas = c.get(c.HOUR_OF_DAY);
            minutos = c.get(c.MINUTE);
            segundos = c.get(c.SECOND);
        }
        iniciarReloj();
    }
    
    private void horaAleatoria(){
        horas = (int) Math.floor(Math.random()*24);
        minutos = (int) Math.floor(Math.random()*60);
        segundos = (int) Math.floor(Math.random()*60);
    }
}
