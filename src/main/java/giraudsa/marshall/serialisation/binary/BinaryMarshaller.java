package giraudsa.marshall.serialisation.binary;

import giraudsa.marshall.annotations.TypeRelation;
import giraudsa.marshall.exception.NotImplementedSerializeException;
import giraudsa.marshall.serialisation.Marshaller;
import giraudsa.marshall.serialisation.binary.ActionBinary.Comportement;
import giraudsa.marshall.serialisation.binary.actions.ActionBinaryCollectionType;
import giraudsa.marshall.serialisation.binary.actions.ActionBinaryDate;
import giraudsa.marshall.serialisation.binary.actions.ActionBinaryDictionaryType;
import giraudsa.marshall.serialisation.binary.actions.ActionBinaryEnum;
import giraudsa.marshall.serialisation.binary.actions.ActionBinaryObject;
import giraudsa.marshall.serialisation.binary.actions.ActionBinaryString;
import giraudsa.marshall.serialisation.binary.actions.ActionBinaryUUID;
import giraudsa.marshall.serialisation.binary.actions.simple.ActionBinaryBoolean;
import giraudsa.marshall.serialisation.binary.actions.simple.ActionBinaryByte;
import giraudsa.marshall.serialisation.binary.actions.simple.ActionBinaryChar;
import giraudsa.marshall.serialisation.binary.actions.simple.ActionBinaryDouble;
import giraudsa.marshall.serialisation.binary.actions.simple.ActionBinaryFloat;
import giraudsa.marshall.serialisation.binary.actions.simple.ActionBinaryInteger;
import giraudsa.marshall.serialisation.binary.actions.simple.ActionBinaryLong;
import giraudsa.marshall.serialisation.binary.actions.simple.ActionBinaryShort;
import giraudsa.marshall.serialisation.binary.actions.simple.ActionBinaryVoid;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;

import utils.Constants;

public class BinaryMarshaller extends Marshaller{
	boolean isCompleteSerialisation;
	private DataOutputStream output;
	@SuppressWarnings("rawtypes")
	Deque<Comportement> aFaire = new LinkedList<>();
	
	/////METHODES STATICS PUBLICS
	public static <U> void toBinary(U obj, OutputStream  output) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException, NotImplementedSerializeException  {
		try(DataOutputStream stream = new DataOutputStream(output)){
			BinaryMarshaller v = new BinaryMarshaller(stream, false);
			v.marshall(obj);
			stream.flush();
		}
	}

	public static <U> void toCompleteBinary(U obj, OutputStream  output) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NotImplementedSerializeException{
		try(DataOutputStream stream = new DataOutputStream(output)){
			BinaryMarshaller v = new BinaryMarshaller(stream,true);
			v.marshall(obj);
			stream.flush();
		}
	}
	
	private BinaryMarshaller(DataOutputStream  output, boolean isCompleteSerialisation) throws IOException {
		this.output = output;
		this.isCompleteSerialisation = isCompleteSerialisation ;
		output.writeBoolean(isCompleteSerialisation);
		//Sans referencement
		dicoTypeToAction.put(void.class, new ActionBinaryVoid(Boolean.class, this));
		dicoTypeToAction.put(Boolean.class, new ActionBinaryBoolean(Boolean.class, this));
		dicoTypeToAction.put(Integer.class, new ActionBinaryInteger(Integer.class, this));
		dicoTypeToAction.put(Byte.class, new ActionBinaryByte(Byte.class, this));
		dicoTypeToAction.put(Float.class, new ActionBinaryFloat(Float.class, this));
		dicoTypeToAction.put(Double.class, new ActionBinaryDouble(Double.class, this));
		dicoTypeToAction.put(Long.class, new ActionBinaryLong(Long.class, this));
		dicoTypeToAction.put(Short.class, new ActionBinaryShort(Short.class, this));
		dicoTypeToAction.put(Character.class, new ActionBinaryChar(Character.class, this));
		//Avec referencement
		dicoTypeToAction.put(UUID.class, new ActionBinaryUUID(UUID.class, this));
		dicoTypeToAction.put(String.class, new ActionBinaryString(String.class, this));
		dicoTypeToAction.put(Date.class, new ActionBinaryDate<>(Date.class, this));
		dicoTypeToAction.put(Enum.class, new ActionBinaryEnum<>(Enum.class, this));
		dicoTypeToAction.put(Collection.class, new ActionBinaryCollectionType<>(Collection.class, this));
		dicoTypeToAction.put(Map.class, new ActionBinaryDictionaryType<>(Map.class, this));
		dicoTypeToAction.put(Object.class, new ActionBinaryObject<>(Object.class, this));
	}
	


	/////METHODES 
	private <T> void marshall(T obj) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException, NotImplementedSerializeException {
		long debut = System.nanoTime();
		marshallSpecialise(obj, TypeRelation.COMPOSITION, true);
		while(!aFaire.isEmpty()){
			aFaire.pop().evalue();
		}
		long fin = System.nanoTime();
		System.out.println("temps de sérialisation = " + + (fin - debut)/1e9 + "secondes" );
	}
	
	byte[] calculHeader(Object o, TypeRelation relation, boolean laRelationAuraitPuEtreMoinsSpecifique, byte debutHeader, boolean estDejaVu) throws IOException{
		Class<?> typeObj = o.getClass();
		boolean isTypeAutre = debutHeader == Constants.Type.AUTRE;
		int smallId = _getSmallIdAndStockObj(o);
		byte typeOfSmallId = getTypeOfSmallId(smallId);
		debutHeader |= typeOfSmallId;
		boolean isDejaVuTypeObj = true;
		int smallIdTypeObj = 0;
		byte typeOfSmallIdTypeObj = 0;
		if(isTypeAutre){
			if(!estDejaVu){
				if(typeObj.getName().toLowerCase().indexOf("org.hibernate.collection.PersistentBag") != -1) typeObj = ArrayList.class;
				if(typeObj.getName().toLowerCase().indexOf("org.hibernate.collection.PersistentSet") != -1) typeObj = HashSet.class;
				if(typeObj.getName().toLowerCase().indexOf("org.hibernate.collection.PersistentMap") != -1) typeObj = HashMap.class;
				if(typeObj.getName().toLowerCase().indexOf("org.hibernate.collection.PersistentSortedSet") != -1) typeObj = TreeSet.class;
				if(typeObj.getName().toLowerCase().indexOf("org.hibernate.collection.PersistentSortedMap") != -1) typeObj = TreeMap.class;
				
				isDejaVuTypeObj = isDejaVuType(typeObj);
				smallIdTypeObj = _getSmallIdTypeAndStockType(typeObj);
				typeOfSmallIdTypeObj = getTypeOfSmallIdTypeObj(smallIdTypeObj);
				debutHeader |= typeOfSmallIdTypeObj;
			}
		}
		try(ByteArrayOutputStream byteOut = new ByteArrayOutputStream()){
			DataOutputStream dataOut = new DataOutputStream(byteOut);
			dataOut.writeByte(debutHeader);
			switch (typeOfSmallId) {
			case Constants.SMALL_ID_TYPE.NEXT_IS_SMALL_ID_BYTE:
				dataOut.writeByte((byte)smallId);
				break;
			case Constants.SMALL_ID_TYPE.NEXT_IS_SMALL_ID_SHORT:
				dataOut.writeShort((short)smallId);
				break;
			case Constants.SMALL_ID_TYPE.NEXT_IS_SMALL_ID_INT:
				dataOut.writeInt(smallId);
				break;
			}
			if(isTypeAutre){
			///////write type if necessary
				if(!estDejaVu && laRelationAuraitPuEtreMoinsSpecifique){
					switch (typeOfSmallIdTypeObj) {
					case Constants.Type.CODAGE_BYTE:
						dataOut.writeByte((byte)smallIdTypeObj);
						break;
					case Constants.Type.CODAGE_SHORT:
						dataOut.writeShort((short)smallIdTypeObj);
						break;
					case Constants.Type.CODAGE_INT:
						dataOut.writeInt(smallIdTypeObj);
						break;
					}
					if(!isDejaVuTypeObj){
						dataOut.writeUTF(typeObj.getName());
						
					}
				}
			}
			return byteOut.toByteArray();
		}
	}

	protected <T> void marshallSpecialise(T obj, TypeRelation relation, boolean laRelationAuraitPuEtreMoinsSpecifique) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NotImplementedSerializeException, IOException{
		ActionBinary<?> action = (ActionBinary<?>) getAction(obj);
		action.serialise(obj, relation, laRelationAuraitPuEtreMoinsSpecifique);
	}

	private byte getTypeOfSmallId(int smallId) {
		if( ((int)((byte)smallId) & 0x000000FF) == smallId) return Constants.SMALL_ID_TYPE.NEXT_IS_SMALL_ID_BYTE;
		if( ((int)((short)smallId) & 0x0000FFFF) == smallId) return Constants.SMALL_ID_TYPE.NEXT_IS_SMALL_ID_SHORT;
		return Constants.SMALL_ID_TYPE.NEXT_IS_SMALL_ID_INT;
	}
	
	private byte getTypeOfSmallIdTypeObj(int smallId) {
		if( ((int)((byte)smallId) & 0x000000FF) == smallId) return Constants.Type.CODAGE_BYTE;
		if( ((int)((short)smallId) & 0x0000FFFF) == smallId) return Constants.Type.CODAGE_SHORT;
		return Constants.Type.CODAGE_INT;
	}
	
	//////////
	boolean writeBoolean(boolean v) throws IOException {
		output.writeBoolean(v);
		return v;
	}
	void writeByte(byte v) throws IOException {
		output.writeByte((int)v);
	}
	void writeByteArray(byte[] v) throws IOException{
		output.write(v);
	}
	void writeShort(short v) throws IOException {
		output.writeShort((int)v);
	}
	void writeChar(char v) throws IOException {
		output.writeChar((int)v);
	}
	void writeInt(int v) throws IOException {
		output.writeInt(v);
	}
	void writeLong(long v) throws IOException {
		output.writeLong(v);
	}
	void writeFloat(float v) throws IOException {
		output.writeFloat(v);
	}
	void writeDouble(double v) throws IOException {
		output.writeDouble(v);
	}
	void writeUTF(String s) throws IOException {
		output.writeUTF(s);
	}
}