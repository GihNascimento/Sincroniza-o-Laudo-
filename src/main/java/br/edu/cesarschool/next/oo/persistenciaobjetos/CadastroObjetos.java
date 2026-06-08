package br.edu.cesarschool.next.oo.persistenciaobjetos;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CadastroObjetos {

    private static final String BASE_DIR = System.getProperty("user.home") + File.separator + "cadastro_bolsa";
    private final String dirPath;

    public CadastroObjetos(Class<?> tipo) {
        dirPath = BASE_DIR + File.separator + tipo.getSimpleName();
        new File(dirPath).mkdirs();
    }

    private File arquivo(String id) {
        String safeId = id.replaceAll("[/\\\\:*?\"<>|]", "_");
        return new File(dirPath + File.separator + safeId + ".dat");
    }

    public Object buscar(String id) {
        File f = arquivo(id);
        if (!f.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    public void incluir(Serializable obj, String id) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arquivo(id)))) {
            oos.writeObject(obj);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao incluir: " + e.getMessage());
        }
    }

    public void alterar(Serializable obj, String id) {
        incluir(obj, id);
    }

    public void excluir(String id) {
        arquivo(id).delete();
    }

    public Object[] buscarTodos() {
        File dir = new File(dirPath);
        File[] arquivos = dir.listFiles((d, name) -> name.endsWith(".dat"));
        if (arquivos == null || arquivos.length == 0) return new Object[0];
        Arrays.sort(arquivos);
        List<Object> lista = new ArrayList<>();
        for (File f : arquivos) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                lista.add(ois.readObject());
            } catch (Exception ignored) {
            }
        }
        return lista.toArray();
    }
}
