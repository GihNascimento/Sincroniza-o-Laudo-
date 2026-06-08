package br.edu.cs.poo.ac.bolsa.testes;

import java.io.File;

public class TesteGenerico {

    protected void limparDiretorio(String className) {
        String BASE_DIR = System.getProperty("user.home") + File.separator + "cadastro_bolsa";
        File dir = new File(BASE_DIR + File.separator + className);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) f.delete();
            }
        }
    }
}
