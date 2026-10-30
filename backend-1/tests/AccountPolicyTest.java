package br.servihub;
import org.junit.Test;
import static org.junit.Assert.*;
public class AccountPolicyTest {
 @Test public void newPasswordsAreExactlySixAsciiDigits(){
  Auth.validateNewPassword("012345");Auth.validateNewPassword("000000");
  for(String value:new String[]{"12345","1234567","abcdef","12 456","１２３４５６","123456\n"}){
   try{Auth.validateNewPassword(value);fail("Senha invalida aceita");}catch(Common.Failure expected){assertEquals(400,expected.status);}
  }
 }
 @Test public void emailsAreValidatedAndNormalized(){
  assertEquals("cliente@example.test",Auth.validateEmail(" Cliente@Example.test "));
  try{Auth.validateEmail("sem-email");fail();}catch(Common.Failure expected){assertEquals(400,expected.status);}
 }
}
