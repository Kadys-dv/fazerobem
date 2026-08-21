package br.com.ajudamutua.crypto;
public interface SecretProtector {
  String encrypt(String plaintext);
  String decrypt(String ciphertext);
  String keyId();
}
