package br.com.erp.api.notification.application.util;

/**
 * Normaliza o nome da cliente para exibição nos e-mails. Os nomes chegam do checkout em
 * caixa alta (ou com capitalização inconsistente); aqui viram "Title Case": a primeira letra
 * de cada palavra em maiúsculo e o restante em minúsculo
 * (ex.: "LUCAS DE MORAES HADDAD DOS SANTOS" → "Lucas De Moraes Haddad Dos Santos").
 *
 * Os separadores originais (espaços simples ou múltiplos) são preservados.
 */
public final class CustomerNameFormatter {

    private CustomerNameFormatter() {}

    public static String toDisplayName(String name) {
        if (name == null || name.isBlank()) return "";

        StringBuilder result = new StringBuilder(name.length());
        boolean startOfWord = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isWhitespace(c)) {
                result.append(c);
                startOfWord = true;
            } else if (startOfWord) {
                result.append(Character.toUpperCase(c));
                startOfWord = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
