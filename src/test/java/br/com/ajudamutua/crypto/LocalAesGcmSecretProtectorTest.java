package br.com.ajudamutua.crypto; import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class LocalAesGcmSecretProtectorTest { @Test void roundTrip(){var c=new LocalAesGcmSecretProtector("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=","test-v1");String enc=c.encrypt("12345678901");assertNotEquals("12345678901",enc);assertEquals("12345678901",c.decrypt(enc));} }
