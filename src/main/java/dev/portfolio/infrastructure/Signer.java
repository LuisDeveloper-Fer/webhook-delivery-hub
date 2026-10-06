package dev.portfolio.infrastructure;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
public final class Signer {
 private Signer(){}
 public static String sign(String secret,String timestamp,String body){try{
  var mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
  return HexFormat.of().formatHex(mac.doFinal((timestamp+"."+body).getBytes(StandardCharsets.UTF_8)));
 }catch(java.security.GeneralSecurityException e){throw new IllegalStateException(e);}}
}
