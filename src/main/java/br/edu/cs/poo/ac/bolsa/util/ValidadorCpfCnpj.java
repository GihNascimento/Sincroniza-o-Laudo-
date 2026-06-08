package br.edu.cs.poo.ac.bolsa.util;

public class ValidadorCpfCnpj {

    public static ResultadoValidacao validarCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) return ResultadoValidacao.NAO_INFORMADO;
        String digits = cpf.replaceAll("[.\\-]", "");
        if (!digits.matches("\\d{11}")) return ResultadoValidacao.FORMATO_INVALIDO;
        if (digits.chars().distinct().count() == 1) return ResultadoValidacao.FORMATO_INVALIDO;
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += (digits.charAt(i) - '0') * (10 - i);
        int r1 = 11 - (soma % 11);
        int d1 = (r1 >= 10) ? 0 : r1;
        if (d1 != (digits.charAt(9) - '0')) return ResultadoValidacao.DV_INVALIDO;
        soma = 0;
        for (int i = 0; i < 10; i++) soma += (digits.charAt(i) - '0') * (11 - i);
        int r2 = 11 - (soma % 11);
        int d2 = (r2 >= 10) ? 0 : r2;
        if (d2 != (digits.charAt(10) - '0')) return ResultadoValidacao.DV_INVALIDO;
        return null;
    }

    public static ResultadoValidacao validarCnpj(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) return ResultadoValidacao.NAO_INFORMADO;
        String digits = cnpj.replaceAll("[.\\-/]", "");
        if (!digits.matches("\\d{14}")) return ResultadoValidacao.FORMATO_INVALIDO;
        if (digits.chars().distinct().count() == 1) return ResultadoValidacao.FORMATO_INVALIDO;
        int[] p1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] p2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) soma += (digits.charAt(i) - '0') * p1[i];
        int r1 = soma % 11;
        int d1 = (r1 < 2) ? 0 : 11 - r1;
        if (d1 != (digits.charAt(12) - '0')) return ResultadoValidacao.DV_INVALIDO;
        soma = 0;
        for (int i = 0; i < 13; i++) soma += (digits.charAt(i) - '0') * p2[i];
        int r2 = soma % 11;
        int d2 = (r2 < 2) ? 0 : 11 - r2;
        if (d2 != (digits.charAt(13) - '0')) return ResultadoValidacao.DV_INVALIDO;
        return null;
    }

    // Método de instância para compatibilidade
    public ResultadoValidacao validar(String cpfOuCnpj) {
        if (cpfOuCnpj == null || cpfOuCnpj.trim().isEmpty()) return ResultadoValidacao.NAO_INFORMADO;
        String digits = cpfOuCnpj.replaceAll("[.\\-/]", "");
        if (digits.length() == 11) return validarCpf(cpfOuCnpj);
        if (digits.length() == 14) return validarCnpj(cpfOuCnpj);
        return ResultadoValidacao.FORMATO_INVALIDO;
    }
}
