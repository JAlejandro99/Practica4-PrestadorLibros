import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConectorBaseDatos {
    Random rd; 
    int idEfim;
    
    public ConectorBaseDatos(){
        rd = new Random();
        try{
            Class.forName("com.mysql.jdbc.Driver");           
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public static void main(String[] args){
        ConectorBaseDatos c = new ConectorBaseDatos();
        String[] cad = new String[2];
        //cad = c.pedirLibro("el muerto");
        //System.out.println(cad[0]+"\n"+cad[1]);
        //c.escribeRegistroBD("8.8.8.8", "18:01", "NombreX", "ClienteCachondo")
        //c.reiniciarBD();
       
        String[] str = c.pedirLibro("192.168.0.1","1:32","Cliente1");
        System.out.println(str[0]);
        System.out.println(str[1]);
    }
    
    public static Connection abrirConexion(){
        Connection conexion = null;
        try {                     
            conexion = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb", "root", "");
        } catch (SQLException ex) {
            Logger.getLogger(ConectorBaseDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conexion;
    }
    
    String[] pedirLibro(String IP, String hora, String nombreCliente){
        String[] ret = new String[2];
        ArrayList<String> ar;
        Connection con = abrirConexion();
        PreparedStatement ps;
        try{
            ar = getLibros();
            int i = rd.nextInt(ar.size());
            ps = con.prepareStatement("SELECT ISBN,nombre,autor,editorial,precio,portada FROM libro WHERE nombre = ? ");
            ps.setString(1,ar.get(i));
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ret[0] = "libro:"+rs.getString("nombre")+","+rs.getString("autor")+","+rs.getString("editorial")+","+rs.getString("precio")+",";
                ret[1] = rs.getString("portada");        
                i = Integer.parseInt(rs.getString("ISBN"));
            }
            con.close();     
            executeUpdate("DELETE FROM libro WHERE ISBN = "+i);
            executeUpdate("INSERT INTO usuario (IP, nombre) VALUES('"+IP+"','"+nombreCliente+"')");
            executeUpdate("INSERT INTO pedido (idPedido,fecha,hora_inicio) VALUES ("+i+",'2021-05-07','"+hora+"')");
            
        }catch(Exception e){
            System.out.println(e);
        }
        return ret;
    }
    public void reiniciarBD(){
        Connection con = abrirConexion();
        PreparedStatement ps;
        try {         
            executeUpdate("DELETE from libro; ");
            executeUpdate("alter table libro AUTO_INCREMENT = 1");
            executeUpdate("DELETE from pedido; ");
            executeUpdate("DELETE from usuario; ");
            executeUpdate("alter table usuario AUTO_INCREMENT = 1");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada) VALUES(\"autor1\", \"Delfin\", \"el muerto\", \"100.75\",\"imagen1.jpg\")");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada) VALUES(\"autor2\", \"Picasso\", \"Cien años de soledad\", \"134.34\",\"imagen2.jpg\")");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada) VALUES(\"autor3\", \"Solman\", \"Hush hush\", \"53.36\",\"imagen3.jpg\");");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada) VALUES(\"autor4\", \"Playa\", \"Malvado Conejito\", \"234.25\",\"imagen4.jpg\");\n");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada) VALUES(\"autor5\", \"Ariel\", \"El Principito\", \"354.65\",\"imagen5.jpg\");");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada) VALUES(\"autor6\", \"Tecnos\", \"Bajo el espino\", \"500.03\",\"imagen6.jpg\");\n");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada) VALUES(\"autor7\", \"Alianza\", \"Harry Potter\", \"68.68\",\"imagen7.jpg\");");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada) VALUES(\"autor8\", \"Akal\", \"Crespusculo\", \"143.96\",\"imagen8.jpg\");");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada) VALUES(\"autor9\", \"Sintesis\", \"Cincuenta sombras de Grey\", \"111.22\",\"imagen9.jpg\");");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada) VALUES(\"autor10\", \"Aranzadi\", \"Quijote\", \"123.12\",\"imagen10.jpg\");");
            con.close();   
        } catch (SQLException ex) {
            Logger.getLogger(ConectorBaseDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList getLibros(){
        ArrayList<String> libros = new ArrayList();
        Connection con = abrirConexion();
        PreparedStatement ps;
        try{
            ps = con.prepareStatement("SELECT nombre FROM libro");
            ResultSet res; 
            res = ps.executeQuery();
            while(res.next())
                libros.add(res.getString("nombre"));
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConectorBaseDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return libros;
    }
    
    public boolean esLibrosVacio(){
        boolean des=false;
        Connection con = abrirConexion();
        PreparedStatement ps;
        try{
            ps = con.prepareStatement("SELECT * FROM libro");
            ResultSet res; 
            res = ps.executeQuery();          
     
            if(res.next())
                des=false;
            else
                des=true;
        } catch (SQLException ex) {
            Logger.getLogger(ConectorBaseDatos.class.getName()).log(Level.SEVERE, null, ex);
        }    
        return des;
    }
    
    public void executeUpdate(String update) {
        Connection connection = abrirConexion();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(update);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                statement.close();
            } catch (Exception e) {
            }
            try {
                connection.close();
            } catch (Exception e) {
            }
        }
    }
    
    public ResultSet executeQuery(String query) {
        Connection connection = abrirConexion();
        Statement statement = null;
        ResultSet set = null;
        try {
            statement = connection.createStatement();
            set = statement.executeQuery(query);
            return set;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { // Close in order: ResultSet, Statement, Connection.
            try {
                set.close();
            } catch (Exception e) {
            }
            try {
                statement.close();
            } catch (Exception e) {
            }
            try {
                connection.close();
            } catch (Exception e) {
            }
        }
        return null;
    }
}

