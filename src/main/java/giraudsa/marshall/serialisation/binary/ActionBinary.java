package giraudsa.marshall.serialisation.binary;

import giraudsa.marshall.exception.MarshallExeption;
import giraudsa.marshall.exception.NotImplementedSerializeException;
import giraudsa.marshall.serialisation.ActionAbstrait;
import giraudsa.marshall.serialisation.Marshaller;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.champ.FieldInformations;
import utils.headers.Header;


public abstract class ActionBinary<T> extends ActionAbstrait<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActionBinary.class);
	
	protected ActionBinary(){
		super();
	}
	
	protected Class<?> getTypeObjProblemeHibernate(Object object) {
		if(object == null)
			return void.class;
		return object.getClass();
	}
	
	@Override
	protected boolean isTypeDevinable(Marshaller marshaller, Object value, FieldInformations fieldInformations) {
		return fieldInformations.isTypeDevinable(value);
	}
	
	protected BinaryMarshaller getBinaryMarshaller(Marshaller marshaller){
		return (BinaryMarshaller)marshaller;
	}
	
	protected boolean writeHeaders(Marshaller marshaller, T objetASerialiser, FieldInformations fieldInformations) throws MarshallExeption, IOException{
		Class<?> typeObj = getTypeObjProblemeHibernate(objetASerialiser);
		boolean isDejaVu = isDejaVu(marshaller, objetASerialiser);
		boolean isTypeDevinable = isTypeDevinable(marshaller, objetASerialiser, fieldInformations);
		boolean isDejaVuType = isDejaVuType(marshaller, typeObj);
		int smallId = getSmallIdAndStockObj(marshaller, objetASerialiser);
		short smallIdType = getSmallIdTypeAndStockType(marshaller, typeObj);
		Header<?> header = Header.getHeader(isDejaVu, isTypeDevinable, smallId, smallIdType);
		header.write(getOutput(marshaller), smallId, smallIdType, isDejaVuType, typeObj);
		return isDejaVu;
	}
	
	protected DataOutput getOutput(Marshaller marshaller){
		return getBinaryMarshaller(marshaller).output;
	}

	protected void writeBoolean(Marshaller marshaller, boolean v) throws IOException {
		getBinaryMarshaller(marshaller).writeBoolean(v);
	}
	protected void writeByte(Marshaller marshaller, byte v) throws IOException {
		getBinaryMarshaller(marshaller).writeByte(v);
	}
	protected void writeByteArray(Marshaller marshaller, byte[] v) throws IOException {
		getBinaryMarshaller(marshaller).writeByteArray(v);
	}
	protected void writeShort(Marshaller marshaller, short v) throws IOException {
		getBinaryMarshaller(marshaller).writeShort(v);
	}
	protected void writeChar(Marshaller marshaller, char v) throws IOException {
		getBinaryMarshaller(marshaller).writeChar(v);
	}
	protected void writeInt(Marshaller marshaller, int v) throws IOException {
		getBinaryMarshaller(marshaller).writeInt(v);
	}
	protected void writeLong(Marshaller marshaller, long v) throws IOException {
		getBinaryMarshaller(marshaller).writeLong(v);
	}
	protected void writeFloat(Marshaller marshaller, float v) throws IOException {
		getBinaryMarshaller(marshaller).writeFloat(v);
	}
	protected void writeDouble(Marshaller marshaller, double v) throws IOException {
		getBinaryMarshaller(marshaller).writeDouble(v);
	}
	protected void writeUTF(Marshaller marshaller, String s) throws IOException {
		getBinaryMarshaller(marshaller).writeUTF(s);
	}
	protected void writeNull(Marshaller marshaller) throws IOException{
		writeByte(marshaller, (byte) 0);
	}
	
	@SuppressWarnings("unchecked")
	@Override protected void marshall(Marshaller marshaller, Object objetASerialiser, FieldInformations fieldInformation) throws MarshallExeption{
		try {
			boolean isDejaVu = writeHeaders(marshaller, (T) objetASerialiser, fieldInformation);
			augmenteProdondeur(marshaller);
			pushComportement(marshaller, new ComportementDiminueProfondeur());
			ecritValeur(marshaller, (T) objetASerialiser, fieldInformation, isDejaVu);
		} catch (MarshallExeption | IOException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | NotImplementedSerializeException e) {
			LOGGER.error("problème à la sérialisation de l'objet " + objetASerialiser.toString(), e);
			throw new MarshallExeption(e);
		}
		
	}

	protected abstract void ecritValeur(Marshaller marshaller, T obj, FieldInformations fieldInformation, boolean isDejaVu) throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, NotImplementedSerializeException, MarshallExeption;
	
	protected int getSmallIdUUIDAndStockUUID(Marshaller marshaller, UUID id) {
		return getBinaryMarshaller(marshaller).getSmallIdAndStockUUID(id);
	}
	protected boolean isDejaVuUUID(Marshaller marshaller, UUID id) {
		return getBinaryMarshaller(marshaller).isDejaVuUUID(id);
	}
	
	protected int getSmallIdStringAndStockString(Marshaller marshaller, String string) {
		return getBinaryMarshaller(marshaller).getSmallIdAndStockString(string);
	}
	protected boolean isDejaVuString(Marshaller marshaller, String string) {
		return getBinaryMarshaller(marshaller).isDejaVuString(string);
	}
	protected int getSmallIdDateAndStockDate(Marshaller marshaller, Date date) {
		return getBinaryMarshaller(marshaller).getSmallIdAndStockDate(date);
	}

	protected boolean isDejaVuDate(Marshaller marshaller, Date date) {
		return getBinaryMarshaller(marshaller).isDejaVuDate(date);
	}
	protected short getSmallIdTypeAndStockType(Marshaller marshaller, Class<?> typeObj) {
		return getBinaryMarshaller(marshaller).getSmallIdTypeAndStockType(typeObj);
	}

	protected int getSmallIdAndStockObj(Marshaller marshaller, Object o) {
		return getBinaryMarshaller(marshaller).getSmallIdAndStockObj(o);
	}

	protected boolean isDejaVuType(Marshaller marshaller, Class<?> typeObj) {
		return getBinaryMarshaller(marshaller).isDejaVuType(typeObj);
	}
	@Override
	protected <U> boolean isDejaVu(Marshaller marshaller, U objet) {
		return getBinaryMarshaller(marshaller).isSmallIdDefined(objet);
	}
	
	
	@Override
	protected <V> boolean aTraiter(Marshaller marshaller, V value, FieldInformations f){
		return true;
	}
	
	protected class ComportementDiminueProfondeur extends Comportement{
		
		@Override
		protected void evalue(Marshaller marshaller){
			diminueProfondeur(marshaller);
		}
	}
}
