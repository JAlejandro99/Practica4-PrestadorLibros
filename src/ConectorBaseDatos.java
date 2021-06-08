import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConectorBaseDatos {
    Random rd;
    int idEfim;
    String namedb;
    int numPedido;
    int numSesion;
    
    public ConectorBaseDatos(int selectBase){
        rd = new Random();
        numPedido = 1;
        numSesion = 1;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            if(selectBase==0)
                namedb="";
            else if (selectBase==1)
                namedb="2";
            else
                namedb="B";
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public static void main(String[] args){
        ConectorBaseDatos c = new ConectorBaseDatos(0);
        String[] cad = new String[2];
        //cad = c.pedirLibro("el muerto");
        //System.out.println(cad[0]+"\n"+cad[1]);
        //c.escribeRegistroBD("8.8.8.8", "18:01", "NombreX", "ClienteCachondo")
        //c.reiniciarBD();
       
        //String[] str = c.pedirLibro("192.168.0.1","1:32","Cliente1");
        c.reiniciarBD();
        c.guardaRegistro("03:00:00","8.8.8.8","Cliente2","el muerto");
        System.out.println("Realizado");

    }
    
    public Connection abrirConexion(){
        Connection conexion = null;
        try {                     
            conexion = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb"+namedb, "root", "");
        } catch (SQLException ex) {
            Logger.getLogger(ConectorBaseDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conexion;
    }
    public void guardaRegistro(String hora_ini, String IP, String nombreCliente,String nombreLibro){    
        executeUpdate("INSERT INTO pedido (idPedido,fecha,hora_inicio) VALUES ("+numPedido+",CURRENT_DATE(),'"+hora_ini+"')"); 
        setLibroDisp(nombreLibro,false);
        registraCliente(nombreCliente,IP);  
        numSesion++;         
        numPedido++;       
    }
    private void setLibroDisp(String nombreLibro,boolean disp){
        Connection con = abrirConexion();
        PreparedStatement ps;
        int isbn=0;
        try{
            ps = con.prepareStatement("SELECT ISBN FROM libro WHERE nombre = '"+nombreLibro+"'");
            ResultSet res;
            res = ps.executeQuery();
            
            if(res.next()){
                isbn = Integer.parseInt(res.getString("ISBN"));
                if(disp)
                    executeUpdate("UPDATE libro SET disponibilidad = 1 WHERE ISBN = "+isbn);    
                else
                    executeUpdate("UPDATE libro SET disponibilidad = 0 WHERE ISBN = "+isbn);
                
                executeUpdate("INSERT INTO sesion (id_sesion,Pedido_idPedido, Libro_ISBN) VALUES ("+numSesion+","+numPedido+","+isbn+")");
            }
            
        }catch(Exception e){
            System.out.println(e);       
            numPedido++;  
        }
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
            executeUpdate("UPDATE libro SET disponibilidad = 0 WHERE ISBN = "+i);         
            executeUpdate("INSERT INTO pedido (idPedido,fecha,hora_inicio) VALUES ("+numPedido+",CURRENT_DATE(),'"+hora+"')");
            executeUpdate("INSERT INTO sesion (id_sesion,Pedido_idPedido, Libro_ISBN) VALUES ("+numSesion+","+numPedido+","+i+")");                    
            registraCliente(nombreCliente,IP);
            
            numSesion++;         
            numPedido++;         
        }catch(Exception e){
            System.out.println(e);
        }
        return ret;
    }
    public void regresarLibros(String nombreCliente,String hora_fin){
        int i;
        ArrayList<Integer> ar = new ArrayList();
        Connection con = abrirConexion();
        PreparedStatement ps;
        try{
            ps = con.prepareStatement("SELECT Pedido_idPedido FROM usuariosesion WHERE Usuario_idUsuario = "+getIdCliente(nombreCliente));
            ResultSet res;
            res = ps.executeQuery();
            while(res.next()){
                i = Integer.parseInt(res.getString("Pedido_idPedido"));
                executeUpdate("UPDATE pedido SET hora_fin = '"+hora_fin+"' WHERE idPedido = "+i+" AND hora_fin IS null");
                ar.add(i);
            }
            res.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConectorBaseDatos.class.getName()).log(Level.SEVERE, null, ex);
        } 
               
        Connection con1 = abrirConexion();
        PreparedStatement ps1=null;
        try{
            ResultSet res=null;
            int isbn=0;
            for (int j = 0; j < ar.size(); j++) {
                ps1 = con1.prepareStatement("SELECT Libro_ISBN FROM sesion WHERE Pedido_idPedido = "+ar.get(j));
                res = ps1.executeQuery();
                if(res.next()){
                    isbn = Integer.parseInt(res.getString("Libro_ISBN"));
                    executeUpdate("UPDATE libro SET disponibilidad = 1 WHERE ISBN = "+isbn);
                }
            }                 
            res.close();
            ps1.close();
            con1.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConectorBaseDatos.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
    }
    //Si el cliente existe, regresa su id, en caso contrario regresa -1
    public void registraCliente(String nombreCliente,String IP){
        int ret=0;
        Connection con = abrirConexion();
        PreparedStatement ps;
        try{
            ps = con.prepareStatement("SELECT idUsuario FROM usuario WHERE nombre = '"+nombreCliente+"'");
            ResultSet res;
            res = ps.executeQuery();
     
            if(res.next()){
                ret = Integer.parseInt(res.getString("idUsuario"));
                executeUpdate("INSERT INTO usuariosesion  (id_usuariosesion,Usuario_idUsuario,Pedido_idPedido) VALUES ("+numSesion+","+ret+","+numPedido+")");
            }
            else{
                executeUpdate("INSERT INTO usuario (IP, nombre) VALUES('"+IP+"','"+nombreCliente+"')");             
                executeUpdate("INSERT INTO usuariosesion  (id_usuariosesion,Usuario_idUsuario,Pedido_idPedido) VALUES ("+numSesion+","+getIdCliente(nombreCliente)+","+numPedido+")");
            }
            res.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConectorBaseDatos.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    private int getIdCliente(String nombreCliente){
        int rt=0;
        Connection con = abrirConexion();
        PreparedStatement ps;
        try{
            ps = con.prepareStatement("SELECT idUsuario FROM usuario WHERE nombre = '"+nombreCliente+"'");
            ResultSet res;
            res = ps.executeQuery();
            if(res.next())
                rt = Integer.parseInt(res.getString("idUsuario"));
            res.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(ConectorBaseDatos.class.getName()).log(Level.SEVERE, null, ex);
        }  
        return rt;
    }
    public void reiniciarBD(){
        Connection con = abrirConexion();
        PreparedStatement ps;
        try {         
            executeUpdate("DELETE from usuariosesion; ");
            executeUpdate("DELETE from sesion; ");            
            executeUpdate("DELETE from libro; ");
            executeUpdate("alter table libro AUTO_INCREMENT = 1");
            executeUpdate("DELETE from pedido; ");
            executeUpdate("alter table pedido AUTO_INCREMENT = 1");
            executeUpdate("DELETE from usuario; ");
            executeUpdate("alter table usuario AUTO_INCREMENT = 1");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada,disponibilidad) VALUES(\"autor1\", \"Delfin\", \"el muerto\", \"100.75\",\"imagen1.jpg\",1)");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada,disponibilidad) VALUES(\"autor2\", \"Picasso\", \"Cien años de soledad\", \"134.34\",\"imagen2.jpg\",1)");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada,disponibilidad) VALUES(\"autor3\", \"Solman\", \"Hush hush\", \"53.36\",\"imagen3.jpg\",1);");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada,disponibilidad) VALUES(\"autor4\", \"Playa\", \"Malvado Conejito\", \"234.25\",\"imagen4.jpg\",1);\n");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada,disponibilidad) VALUES(\"autor5\", \"Ariel\", \"El Principito\", \"354.65\",\"imagen5.jpg\",1);");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada,disponibilidad) VALUES(\"autor6\", \"Tecnos\", \"Bajo el espino\", \"500.03\",\"imagen6.jpg\",1);\n");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada,disponibilidad) VALUES(\"autor7\", \"Alianza\", \"Harry Potter\", \"68.68\",\"imagen7.jpg\",1);");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada,disponibilidad) VALUES(\"autor8\", \"Akal\", \"Crespusculo\", \"143.96\",\"imagen8.jpg\",1);");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada,disponibilidad) VALUES(\"autor9\", \"Sintesis\", \"Cincuenta sombras de Grey\", \"111.22\",\"imagen9.jpg\",1);");
            executeUpdate("INSERT INTO libro (autor,editorial,nombre,precio,portada,disponibilidad) VALUES(\"autor10\", \"Aranzadi\", \"Quijote\", \"123.12\",\"imagen10.jpg\",1);");
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
            ps = con.prepareStatement("SELECT nombre FROM libro WHERE disponibilidad = 1");
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
            ps = con.prepareStatement("SELECT * FROM libro WHERE disponibilidad = 1");
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

