import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class HillsCypher {
    public static void main(String[] args) {
        var scanner = new Scanner(System.in);

        System.out.println("Olá, seja bem vindo!");
        System.out.println("Por favor, digite o caminho completo do arquivo .txt de entrada:");
        System.out.println("[EXEMPLO] C:\\{{entrada}}.txt");

        String inputFilePath = scanner.nextLine();

        System.out.println("Por favor, digite o caminho completo do arquivo .txt de saída:");
        System.out.println("[EXEMPLO] C:\\{{saida}}.txt");

        String outputFilePath = scanner.nextLine();

        var key = "BAAAAAABAAAAAADHAAAACFAAAAAABAAAAAAJ";

        var n = (int) Math.sqrt(key.length());

        System.out.println("Você deseja [E]ncriptar ou [D]ecriptar o arquivo?");
        var choice = scanner.nextLine().toUpperCase();

        var message = new StringBuilder();

        try (Scanner fileScanner = new Scanner(new File(inputFilePath))) {
            while (fileScanner.hasNextLine()) {
                message.append(fileScanner.nextLine().toUpperCase());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Erro: Arquivo de entrada não encontrado em: " + inputFilePath);
            scanner.close();
            return;
        }

        var result = "";
        var keyMatrix = new int[n][n];
        getKeyMatrix(key, keyMatrix, n);

        if (choice.equals("E")) {
            result = hillCipherEncrypt(message.toString(), key, n);
            try (FileWriter writer = new FileWriter(outputFilePath)) {
                writer.write(result);
                System.out.println("Texto gravado com sucesso em: " + outputFilePath);
            } catch (IOException e) {
                System.out.println("Erro ao escrever no arquivo de saída: " + outputFilePath);
            }
        } else if (choice.equals("D")) {
            var determinant = calculateDeterminant(keyMatrix, n);
            var detInverse = modularInverse(determinant % 26);

                var adjugateMatrix = adjugate(keyMatrix, n);
                var inverseMatrix = new int[n][n];

                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        inverseMatrix[i][j] = (adjugateMatrix[i][j] * detInverse) % 26;
                        if (inverseMatrix[i][j] < 0) {
                            inverseMatrix[i][j] += 26;
                        }
                    }
                }
                result = hillCipherDecrypt(message.toString(), inverseMatrix, n);

                try (FileWriter writer = new FileWriter(outputFilePath)) {
                    writer.write(result);
                    System.out.println("Texto gravado com sucesso em: " + outputFilePath);
                } catch (IOException e) {
                    System.out.println("Erro ao escrever no arquivo de saída: " + outputFilePath);
            }
        } else {
            System.out.println("Opção inválida. Por favor, escolha 'E' para encriptar ou 'D' para decriptar.");
        }

        scanner.close();
    }

    static String hillCipherDecrypt(String message, int[][] inverseKeyMatrix, int n) {
        var plainText = new StringBuilder();
        message = formatMessage(message, n);

        for (int i = 0; i < message.length(); i += n) {
            var cipherBlock = new int[n][1];
            var plainBlock = new int[n][1];

            for (int j = 0; j < n; j++) {
                cipherBlock[j][0] = (message.charAt(i + j)) % 65;
            }

            decrypt(plainBlock, inverseKeyMatrix, cipherBlock, n);

            for (int j = 0; j < n; j++) {
                plainText.append((char) (plainBlock[j][0] + 65));
            }
        }
        return plainText.toString();
    }

    static void decrypt(int[][] plainBlock, int[][] inverseKeyMatrix, int[][] cipherBlock, int n) {
        for (int i = 0; i < n; i++) {
            plainBlock[i][0] = 0;
            for (int j = 0; j < n; j++) {
                plainBlock[i][0] += inverseKeyMatrix[i][j] * cipherBlock[j][0];
            }
            plainBlock[i][0] %= 26;
        }
    }

    static void getKeyMatrix(String key, int[][] keyMatrix, int n) {
        var k = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                keyMatrix[i][j] = (key.charAt(k)) % 65;
                k++;
            }
        }
    }

    static void encrypt(int[][] cipherBlock, int[][] keyMatrix, int[][] messageBlock, int n) {
        for (int i = 0; i < n; i++) {
            cipherBlock[i][0] = 0;
            for (int j = 0; j < n; j++) {
                cipherBlock[i][0] += keyMatrix[i][j] * messageBlock[j][0];
            }
            cipherBlock[i][0] %= 26;
        }
    }

    static String hillCipherEncrypt(String message, String key, int n) {
        var keyMatrix = new int[n][n];

        getKeyMatrix(key, keyMatrix, n);

        var cipherText = new StringBuilder();
        message = formatMessage(message, n);

        for (int i = 0; i < message.length(); i += n) {
            var messageBlock = new int[n][1];
            var cipherBlock = new int[n][1];

            for (int j = 0; j < n; j++) {
                messageBlock[j][0] = (message.charAt(i + j)) % 65;
            }

            encrypt(cipherBlock, keyMatrix, messageBlock, n);

            for (int j = 0; j < n; j++) {
                cipherText.append((char) (cipherBlock[j][0] + 65));
            }
        }

        return cipherText.toString();
    }

    private static String formatMessage(String message, int n) {
        var messageConcat = new StringBuilder(message);

        while (messageConcat.length() % n != 0) {
            messageConcat.append('X');
        }
        message = messageConcat.toString();
        return message;
    }

    static int calculateDeterminant(int[][] matrix, int n) {
        if (n == 2) {
            return (matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]);
        }
        var det = 0;
        var sign = 1;
        for (int i = 0; i < n; i++) {
            var subMatrix = new int[n - 1][n - 1];
            for (int j = 1; j < n; j++) {
                for (int k = 0, col = 0; k < n; k++) {
                    if (k != i) {
                        subMatrix[j - 1][col++] = matrix[j][k];
                    }
                }
            }
            det += sign * matrix[0][i] * calculateDeterminant(subMatrix, n - 1);
            sign = -sign;
        }
        return det;
    }

    static int modularInverse(int a) {
        a = a % 26;
        for (int x = 1; x < 26; x++) {
            if ((a * x) % 26 == 1) {
                return x;
            }
        }
        return -1; // quando a inversa não existe
    }

    static int[][] adjugate(int[][] matrix, int n) {
        int[][] adj = new int[n][n];
        if (n == 1) {
            adj[0][0] = 1;
            return adj;
        }
        int sign = 1;
        int[][] temp = new int[n - 1][n - 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int row = 0, col = 0;
                for (int row_ = 0; row_ < n; row_++) {
                    for (int col_ = 0; col_ < n; col_++) {
                        if (row_ != i && col_ != j) {
                            temp[row][col++] = matrix[row_][col_];
                            if (col == n - 1) {
                                col = 0;
                                row++;
                            }
                        }
                    }
                }
                sign = ((i + j) % 2 == 0) ? 1 : -1;
                adj[j][i] = (sign * calculateDeterminant(temp, n - 1));
            }
        }
        return adj;
    }
}