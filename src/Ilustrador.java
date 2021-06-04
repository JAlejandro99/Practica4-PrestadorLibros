import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Ilustrador extends JPanel{
    int ancho,alto;
    String NOMBRE_IMAGE;
    public Ilustrador(int ancho, int alto, String img){
        super();
        this.setBounds(420,180,ancho,alto);
        this.ancho = ancho;
        this.alto = alto;
        this.NOMBRE_IMAGE = img;
        repaint();
    }
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        try{
            BufferedImage imagen = resize(ImageIO.read(new File(NOMBRE_IMAGE)),(Graphics2D)g);
            g.drawImage(imagen,0,0,this);
        }catch(IOException e){}
    }
    public BufferedImage resize(BufferedImage img, Graphics2D g) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(ancho, alto, img.getType());
        g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, ancho, alto, 0, 0, w, h, null);
        g.dispose();
        return dimg;
    }
    public void dibujar(String img){
        this.NOMBRE_IMAGE = img;
        repaint();
    }
    /*public static void main(String[] args){
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame ventana = new JFrame("Dibujando imagen");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setBackground(Color.white);
        ventana.setSize(300,300);
        Ilustrador panel = new Ilustrador(200,150,"huskies.jpg");
        ventana.add(panel);
        ventana.setVisible(true);
    }*/
}