package sourceafis.simple;

import java.io.Serializable;

import org.w3c.dom.Element;

import sourceafis.templates.CompactFormat;
import sourceafis.templates.IsoFormat;
import sourceafis.templates.SerializedFormat;
import sourceafis.templates.Template;
import sourceafis.templates.XmlFormat;

/**
 * Collection of fingerprint-related information.
 * 
 * This class contains basic information ({@link #setImage image},
 * {@link #getTemplate template}) about the fingerprint that is used by
 * SourceAFIS to perform template extraction and fingerprint matching. If you
 * need to attach application-specific information to {@link Fingerprint}
 * object, inherit from this class and add fields as necessary.
 * {@link Fingerprint} objects can be grouped in {@link Person} objects.
 * <p>
 * This class is designed to be easy to serialize in order to be stored in
 * binary format (BLOB) in application database, binary or XML files, or sent
 * over network. You can either serialize the whole object or serialize
 * individual properties. You can set some properties to null to exclude them
 * from serialization.
 * 
 * @see Person
 * @serial exclude
 */
@SuppressWarnings("serial")
public class Fingerprint implements Cloneable, Serializable {
	private static final CompactFormat compactFormat = new CompactFormat();
	private static final SerializedFormat serializedFormat = new SerializedFormat();
	private static final IsoFormat isoFormat = new IsoFormat();
	private static final XmlFormat xmlFormat = new XmlFormat();
	private Finger fingerPosition;
	Template decoded;
	private byte[][] image;
	private String sourceName;

	/**
	 * Creates empty Fingerprint object.
	 */
	public Fingerprint() {
	}

	/**
	 * Gets fingerprint image (not implemented in java).
	 * 
	 * This is the raw fingerprint image that was used to
	 * {@link AfisEngine#extract extract} the {@link #getTemplate template} or
	 * other image attached later after extraction. This property is null by
	 * default. See {@link #setImage setImage} for further explanation.
	 * <p>
	 * Return value is not cloned internally. To avoid unwanted sharing, clone
	 * the image data after calling this method.
	 * 
	 * @return fingerprint image or null
	 * @see #setImage setImage
	 * @see #getTemplate getTemplate
	 * @see AfisEngine#extract AfisEngine.extract
	 */
	public byte[][] getImage() {
		return image;
	}

	/**
	 * Sets fingerprint image (not implemented in java).
	 * 
	 * Image must be set before call to {@link AfisEngine#extract
	 * AfisEngine.extract} in order to generate valid {@link #getTemplate
	 * template}. Once the {@link #getTemplate template} is generated,
	 * fingerprint image has only informational meaning and it can be set to
	 * null to save space. It is however recommended to keep the original image
	 * just in case it is needed to regenerate the {@link #getTemplate template}
	 * in future.
	 * <p>
	 * The format of this image is a simple raw 2D array of bytes. Every byte
	 * represents shade of gray from black (0) to white (255). When indexing the
	 * 2D array, Y axis goes first, X axis goes second, e.g. image[y][x].
	 * <p>
	 * Parameter value is not cloned internally. To avoid unwanted sharing,
	 * clone the image data before calling this method.
	 * 
	 * @param newImage
	 *            new fingerprint image for this fingerprint or null
	 * @see #getImage getImage
	 * @see #setTemplate setTemplate
	 * @see AfisEngine#extract AfisEngine.extract
	 */
	public void setImage(byte[][] newImage) throws Exception {
		if (newImage == null)
			image = null;
		else {
			if (newImage.length < 100 || newImage[0].length < 100)
				throw new Exception("Fingerprint image is too small.");
			image = newImage;
		}
	}

	/**
	 * Gets fingerprint template.
	 * 
	 * Fingerprint template is generated by {@link AfisEngine#extract
	 * AfisEngine.extract}. Alternatively it can be other template assigned for
	 * example after deserialization. This property is null by default.
	 * <p>
	 * Fingerprint template is an abstract model of the fingerprint that is
	 * serialized in a very compact binary format (up to a few KB). Templates
	 * are better than fingerprint images, because they require less space and
	 * they are easier to match than images. To generate the template, pass
	 * {@link Fingerprint} object with valid {@link #setImage image} to
	 * {@link AfisEngine#extract AfisEngine.extract}. Valid template is required
	 * by {@link AfisEngine#verify AfisEngine.verify} and
	 * {@link AfisEngine#identify AfisEngine.identify}.
	 * <p>
	 * Format of the template may change in later versions of SourceAFIS.
	 * Applications are recommended to keep the original {@link #setImage image}
	 * in order to be able to regenerate the template. Value of
	 * {@link #setFinger finger} property is not automatically stored in the
	 * template. It must be stored separately.
	 * 
	 * @return fingerprint's current template in native format
	 * @see #setTemplate setTemplate
	 * @see AfisEngine#extract AfisEngine.extract
	 * @see #getIsoTemplate getIsoTemplate
	 * @see #getXmlTemplate getXmlTemplate
	 */
	public byte[] getTemplate() {
		return decoded != null ? compactFormat.exportTemplate(serializedFormat
				.importTemplate(decoded)) : null;
	}

	/**
	 * Sets fingerprint template.
	 * 
	 * For complete description, see {@link #getTemplate getTemplate}. Most
	 * applications don't need to call this method. Template is usually
	 * automatically set by {@link AfisEngine#extract AfisEngine.extract}.
	 * Application might wish to set template explicitly for example when
	 * deserializing the fingerprint.
	 * 
	 * @param value
	 *            fingerprint's new template in native format
	 * @see #getTemplate getTemplate
	 * @see AfisEngine#extract AfisEngine.extract
	 * @see #setIsoTemplate setIsoTemplate
	 * @see #setXmlTemplate setXmlTemplate
	 */
	public void setTemplate(byte[] value) {
		decoded = value != null ? serializedFormat.exportTemplate(compactFormat
				.importTemplate(value)) : null;
	}

	/**
	 * Gets fingerprint template in standard ISO format.
	 * 
	 * This is the value of {@link #getTemplate getTemplate} converted to
	 * standard ISO/IEC 19794-2 (2005) format. The result is null if
	 * {@link #getTemplate getTemplate} returns null.
	 * <p>
	 * Use this method together with {@link #setIsoTemplate setIsoTemplate} for
	 * two-way exchange of fingerprint templates with other biometric systems.
	 * For general use in SourceAFIS, use {@link #getTemplate getTemplate}
	 * method which returns native template that is fine-tuned for best accuracy
	 * and performance in SourceAFIS.
	 * <p>
	 * SourceAFIS contains partial implementation of ISO/IEC 19794-2 (2005)
	 * standard. Multi-fingerprint ISO templates must be split into individual
	 * fingerprints before they are used in SourceAFIS. Value of
	 * {@link #setFinger finger} property is not automatically stored in the ISO
	 * template. It must be decoded separately.
	 * 
	 * @return fingerprint's current template in standard ISO format
	 * @see #setIsoTemplate setIsoTemplate
	 * @see AfisEngine#extract AfisEngine.extract
	 * @see #getTemplate getTemplate
	 * @see #getXmlTemplate getXmlTemplate
	 */
	public byte[] getIsoTemplate() {
		return decoded != null ? isoFormat.exportTemplate(serializedFormat
				.importTemplate(decoded)) : null;
	}

	/**
	 * Sets fingerprint template in standard ISO format.
	 * 
	 * The parameter is converted from standard ISO/IEC 19794-2 (2005) format to
	 * native format and passed to {@link #setTemplate setTemplate}. If the
	 * parameter is null, {@link #setTemplate setTemplate} will be called with
	 * null parameter. See {@link #getIsoTemplate getIsoTemplate} for details.
	 * 
	 * @param value
	 *            fingerprint's new template in standard ISO format
	 * @see #getIsoTemplate getIsoTemplate
	 * @see AfisEngine#extract AfisEngine.extract
	 * @see #setTemplate setTemplate
	 * @see #setXmlTemplate setXmlTemplate
	 */
	public void setIsoTemplate(byte[] value) {
		decoded = value != null ? serializedFormat.exportTemplate(isoFormat
				.importTemplate(value)) : null;
	}

	/**
	 * Gets fingerprint template in readable XML format.
	 * 
	 * This is the value of {@link #getTemplate getTemplate} converted to
	 * SourceAFIS XML template format. The result is null if
	 * {@link #getTemplate getTemplate} returns null.
	 * <p>
	 * Use XML template format where clean data format is more important than
	 * compact and fast encoding. XML templates are suitable for XML-based data
	 * exchange, encoding of multiple fingerprints along with accompanying data
	 * into single XML file, and for debugging and logging purposes.
	 * <p>
	 * Value of {@link #setFinger finger} property is not automatically stored
	 * in the XML template. It must be stored separately.
	 * 
	 * @return fingerprint's current template in SourceAFIS XML format
	 * @see #setXmlTemplate setXmlTemplate
	 * @see AfisEngine#extract AfisEngine.extract
	 * @see #getTemplate getTemplate
	 * @see #getIsoTemplate getIsoTemplate
	 */
	public Element getXmlTemplate() {
		return decoded != null ? xmlFormat.exportTemplate(serializedFormat
				.importTemplate(decoded)) : null;
	}

	/**
	 * Sets fingerprint template in readable XML format.
	 * 
	 * The parameter is converted from SourceAFIS XML template format to native
	 * format and passed to {@link #setTemplate setTemplate}. If the parameter
	 * is null, {@link #setTemplate setTemplate} will be called with null
	 * parameter. See {@link #getXmlTemplate getXmlTemplate} for details.
	 * 
	 * @param value
	 *            fingerprint's new template in SourceAFIS XML format
	 * @see #getXmlTemplate getXmlTemplate
	 * @see AfisEngine#extract AfisEngine.extract
	 * @see #setTemplate setTemplate
	 * @see #setIsoTemplate setIsoTemplate
	 */
	public void setXmlTemplate(Element value) {
		this.decoded = value != null ? serializedFormat
				.exportTemplate(xmlFormat.importTemplate(value)) : null;
	}

	/**
	 * Gets position of the finger on hand.
	 * 
	 * See {@link #setFinger setFinger} for explanation. This method just
	 * retrieves current finger position.
	 * 
	 * @see #setFinger setFinger
	 * @see Finger
	 */
	public Finger getFinger() {
		return fingerPosition;
	}

	/**
	 * Sets position of the finger on hand.
	 * 
	 * Sets finger (thumb to little) and hand (right or left) that was used to
	 * create this fingerprint. Default value {@link Finger#ANY} means
	 * unspecified finger position.
	 * <p>
	 * Finger position is used to speed up matching by skipping fingerprint
	 * pairs with incompatible finger positions. Check {@link Finger}
	 * enumeration for information on how to control this process. Default value
	 * {@link Finger#ANY} disables this behavior.
	 * 
	 * @param value
	 *            new finger position for this fingerprint
	 * @see #getFinger getFinger
	 * @see Finger
	 */
	public void setFinger(Finger value) {
		fingerPosition = value;
	}

	/**
	 * Creates deep copy of the Fingerprint object.
	 * 
	 * @return deep copy of this object
	 */
	@Override
	public Fingerprint clone() {
		Fingerprint fp = new Fingerprint();
		fp.decoded = decoded != null ? decoded.clone() : null;
		fp.fingerPosition = fingerPosition;
		return fp;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
}