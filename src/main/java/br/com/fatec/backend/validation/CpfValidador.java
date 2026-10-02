//package br.com.fatec.backend.validation;
//
//import jakarta.validation.ConstraintValidator;
//import jakarta.validation.ConstraintValidatorContext;
//
//public class CpfValidador implements ConstraintValidator<CpfValido, String> {
//
//    @Override
//    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
//        if (valor == null) {
//            return true;
//        }
//
//        String cpf = valor.replaceAll("\\D", "");
//
//        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
//            return false;
//        }
//
//        return calcularDigito(cpf, 9) == digitoNa(cpf, 9)
//                && calcularDigito(cpf, 10) == digitoNa(cpf, 10);
//    }
//
//    private int calcularDigito(String cpf, int quantidadeDigitos) {
//        int soma = 0;
//        for (int i = 0; i < quantidadeDigitos; i++) {
//            soma += digitoNa(cpf, i) * (quantidadeDigitos + 1 - i);
//        }
//        int resto = soma % 11;
//        return resto < 2 ? 0 : 11 - resto;
//    }
//
//    private int digitoNa(String cpf, int posicao) {
//        return cpf.charAt(posicao) - '0';
//    }
//}