package it.albertus.cyclesmod.common.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HiddenBike {

	private static final long CRC32 = 0xEF30BAE6;

	private static final String DEFLATE_BASE64 = "eNpjY5DQ22DGr57GnMb8jukd0ynmU8zvpBkYGDoYihiiGDwYnBjsNFCBkY0bduCNDYRCQVQ8DKQgAIgbExMVGgBWamOkzM8KtBoAC/woBQ==";

	//	public static Properties getProperties(@NonNull final BikeType target) {
	//		final Properties internal = new Properties();
	//		try (final InputStream is = HiddenBike.class.getResourceAsStream(RESOURCE_NAME)) {
	//			if (is != null) {
	//				internal.load(is);
	//			}
	//			else {
	//				throw new FileNotFoundException(RESOURCE_NAME);
	//			}
	//		}
	//		catch (final IOException e) {
	//			throw new UncheckedIOException("Cannot load resource " + '/' + HiddenBike.class.getPackage().getName().replace('.', '/') + RESOURCE_NAME, e);
	//		}
	//
	//		final Properties properties = new Properties();
	//
	//		final String[] settings = internal.getProperty(Settings.PREFIX).split(",");
	//		if (settings.length != Settings.LENGTH / 2) {
	//			throw new VerifyError("Invalid " + Settings.PREFIX + " array length, expected " + Settings.LENGTH / 2 + " but was " + settings.length);
	//		}
	//		for (final Setting setting : Setting.values()) {
	//			properties.setProperty(target.getDisplacement() + "." + Settings.PREFIX + "." + setting.getKey(), settings[setting.getIndex()]);
	//		}
	//
	//		final String[] gearbox = internal.getProperty(Gearbox.PREFIX).split(",");
	//		if (gearbox.length != Gearbox.LENGTH / 2) {
	//			throw new VerifyError("Invalid " + Gearbox.PREFIX + " array length, expected " + Gearbox.LENGTH / 2 + " but was " + gearbox.length);
	//		}
	//		for (int i = 0; i < gearbox.length; i++) {
	//			properties.setProperty(target.getDisplacement() + "." + Gearbox.PREFIX + "." + i, gearbox[i]);
	//		}
	//
	//		final String[] power = internal.getProperty(Power.PREFIX).split(",");
	//		if (power.length != Power.LENGTH) {
	//			throw new VerifyError("Invalid " + Power.PREFIX + " array length, expected " + Power.LENGTH + " but was " + power.length);
	//		}
	//		for (int i = 0; i < power.length; i++) {
	//			properties.setProperty(target.getDisplacement() + "." + Power.PREFIX + "." + i, power[i]);
	//		}
	//
	//		return properties;
	//	}
	//
	//	public static void main(String[] args) throws IOException {
	//		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	//		try (final InputStream is = Files.newInputStream(Paths.get("d:\\hidden.inf"))) {// HiddenBike.class.getResourceAsStream("HIDDEN.INF")) {
	//			IOUtils.copy(is, baos, 256);
	//		}
	//		System.out.println(baos.toByteArray().length);
	//		Deflater d = new Deflater(9);
	//		System.out.println(d.needsInput());
	//		d.setInput(baos.toByteArray());
	//		d.finish();
	//		System.out.println(d.needsInput());
	//		byte[] buf = new byte[150];
	//		final int len = d.deflate(buf);
	//		System.out.println(len);
	//		byte[] out = Arrays.copyOf(buf, 79);
	//		System.out.println(out.length);
	//		System.out.println(Base64.getEncoder().encodeToString(out));
	//	}

}
