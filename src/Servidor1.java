public class Servidor1 {
    public static void main(String[] args){
        Servidor server = new Servidor(5000,"0",6000);
        new Thread(server).start();
    }//main
}
