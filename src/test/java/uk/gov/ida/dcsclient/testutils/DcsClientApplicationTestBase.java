package uk.gov.ida.dcsclient.testutils;

import io.dropwizard.testing.junit.DropwizardClientRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import sun.misc.BASE64Encoder;
import sun.security.provider.X509Factory;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;
import uk.gov.ida.dcsclient.stubs.StubDcs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

public abstract class DcsClientApplicationTestBase {

    public static final String CERTIFICATE_PATH = "src/test/resources/certificate.cert";
    public static final String PRIVATE_KEY_PATH = "src/test/resources/private_key.pk8";
    public static final long ONE_YEAR = (long) 365 * 24 * 60 * 60;
    private static final StubDcs stubDcsResource = new StubDcs();

    @ClassRule
    public static final DropwizardClientRule stubDcs = new DropwizardClientRule(stubDcsResource);

    @BeforeClass
    public static void setUpClass() throws Exception {
        CertAndKeyGen certGen = new CertAndKeyGen("RSA", "SHA256WithRSA", null);
        certGen.generate(2048);
        X509Certificate certificate = certGen.getSelfCertificate(new X500Name("CN=My Application,O=My Organisation,L=My City,C=DE"), ONE_YEAR);
        RSAPrivateKey privateKey = (RSAPrivateKey) certGen.getPrivateKey();

        FileOutputStream certificateWriter = new FileOutputStream(CERTIFICATE_PATH);
        BASE64Encoder encoder = new BASE64Encoder();
        certificateWriter.write(X509Factory.BEGIN_CERT.getBytes());
        certificateWriter.write('\n');
        encoder.encodeBuffer(certificate.getEncoded(), certificateWriter);
        certificateWriter.write(X509Factory.END_CERT.getBytes());
        certificateWriter.close();

        byte[] privateKeyBytes = privateKey.getEncoded();
        FileOutputStream privateKeyOut = new FileOutputStream(PRIVATE_KEY_PATH);
        privateKeyOut.write(privateKeyBytes);
        privateKeyOut.close();

        stubDcsResource.setUpKeys(CERTIFICATE_PATH, PRIVATE_KEY_PATH);
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        Path certificatePath = Paths.get(CERTIFICATE_PATH);
        Files.delete(certificatePath);
        Path privateKeyPath = Paths.get(PRIVATE_KEY_PATH);
        Files.delete(privateKeyPath);
    }
}
