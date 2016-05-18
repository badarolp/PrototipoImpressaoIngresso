package prototipoimpressaoingresso;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;

import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.Winspool;
import com.sun.jna.platform.win32.Winspool.JOB_INFO_1;
import com.sun.jna.platform.win32.WinspoolUtil;

public class Win32SpoolMonitor extends Thread {
    
    private int countJob;
    private Tela tela;
    
    public Win32SpoolMonitor(Tela tela) {
        this.tela = tela;
    }

    public void run() {
        try {
            this.countJob = 0;
            //String pPrinterName = "HP Color LaserJet CM4730 MFP PCL 6";
            //String pPrinterName = "HP Deskjet D1600 series";
            String pPrinterName = "HP LaserJet P1005";
            HANDLEByReference phPrinter = new HANDLEByReference();
            Winspool.INSTANCE.OpenPrinter(pPrinterName, phPrinter, null);
            // Get change notification handle for the printer
            HANDLE chgObject = Winspool.INSTANCE
                    .FindFirstPrinterChangeNotification(phPrinter.getValue(),
                            Winspool.PRINTER_CHANGE_JOB, 0, null);
            if (chgObject != null) {                
                while (true) {
                    // Wait for the change notification
                    Kernel32.INSTANCE.WaitForSingleObject(chgObject,
                           WinBase.INFINITE);
                    DWORDByReference pdwChange = new DWORDByReference();
                    boolean fcnreturn = Winspool.INSTANCE
                            .FindNextPrinterChangeNotification(chgObject,
                                    pdwChange, null, null);
                    JOB_INFO_1[] jobInfo1 = WinspoolUtil.getJobInfo1(phPrinter);
                    if(jobInfo1 == null) {
                        this.countJob = 0;
                        this.tela.setItens(this.countJob);
                    } else if(this.countJob != jobInfo1.length) {
                        this.countJob = jobInfo1.length;
                        this.tela.setItens(this.countJob);
                    }
                    if (fcnreturn) {
                        for (int i = 0; i < jobInfo1.length; i++) {
                            this.printJobInfo(jobInfo1[i]);
                        }
                        //break;
                    }
                }
                // Close Printer Change Notification handle when finished.
                //Winspool.INSTANCE.FindClosePrinterChangeNotification(chgObject);
            } else {
                // Unable to open printer change notification handle
                getLastError();
            }            
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public int getLastError() {
        int rc = Kernel32.INSTANCE.GetLastError();
        if (rc != 0)
            System.out.println("error: " + rc);
        return rc;
    }

    private void printJobInfo(JOB_INFO_1 jobInfo1) {
        FILETIME lpFileTime = new FILETIME();
        Kernel32.INSTANCE.SystemTimeToFileTime(jobInfo1.Submitted, lpFileTime);
        this.tela.setStatus(jobInfo1.Status);
    }
}