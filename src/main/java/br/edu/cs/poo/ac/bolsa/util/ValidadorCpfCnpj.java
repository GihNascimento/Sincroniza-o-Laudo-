package br.edu.cs.poo.ac.bolsa.util;

public class ValidadorCpfCnpj {

    public ResultadoValidacao validar(String cpfOuCnpj) {
        if (cpfOuCnpj == null || cpfOuCnpj.trim().isEmpty()) {
            return ResultadoValidacao.NAO_INFORMADO;
        }
        String digits = cpfOuCnpj.replaceAll("[.\\-/]", "");
        if (digits.length() == 11) {
            return validarCpf(digits);
        } else if (digits.length() == 14) {
            return validarCnpj(digits);
        }
        return ResultadoValidacao.FORMATO_INVALIDO;
    }

    private ResultadoValidacao validarCpf(String cpf) {
        if (!cpf.matches("\\d{11}")) return ResultadoValidacao.FORMATO_INVALIDO;
        if (cpf.chars().distinct().count() == 1) return ResultadoValidacao.DV_INVALIDO;
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += (cpf.charAt(i) - '0') * (10 - i);
        int r1 = 11 - (soma % 11);
        int d1 = (r1 >= 10) ? 0 : r1;
        if (d1 != (cpf.charAt(9) - '0')) return ResultadoValidacao.DV_INVALIDO;
        soma = 0;
        for (int i = 0; i < 10; i++) soma += (cpf.charAt(i) - '0') * (11 - i);
        int r2 = 11 - (soma % 11);
        int d2 = (r2 >= 10) ? 0 : r2;
        if (d2 != (cpf.charAt(10) - '0')) return ResultadoValidacao.DV_INVALIDO;
        return null;
    }

    private ResultadoValidacao validarCnpj(String cnpj) {
        if (!cnpj.matches("\\d{14}")) return ResultadoValidacao.FORMATO_INVALIDO;
        if (cnpj.chars().distinct().count() == 1) return ResultadoValidacao.DV_INVALIDO;
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) soma += (cnpj.charAt(i) - '0') * pesos1[i];
        int r1 = soma % 11;
        int d1 = (r1 < 2) ? 0 : 11 - r1;
        if (d1 != (cnpj.charAt(12) - '0')) return ResultadoValidacao.DV_INVALIDO;
        soma = 0;
        for (int i = 0; i < 13; i++) soma += (cnpj.charAt(i) - '0') * pesos2[i];
        int r2 = soma % 11;
        int d2 = (r2 < 2) ? 0 : 11 - r2;
        if (d2 != (cnpj.charAt(13) - '0')) return ResultadoValidacao.DV_INVALIDO;
        return null;
    }
}
