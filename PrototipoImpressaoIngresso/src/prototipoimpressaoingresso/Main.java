package prototipoimpressaoingresso;

public class Main {
    public static void main(String[] args) {
        //Win32SpoolMonitor spool = new Win32SpoolMonitor();
        //spool.start();
        Tela tela = new Tela();
        tela.setVisible(true);
        new Win32SpoolMonitor(tela).start();
    }
}