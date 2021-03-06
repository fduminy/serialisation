package giraudsa.marshall.serialisation.binary.actions;

import giraudsa.marshall.exception.MarshallExeption;
import giraudsa.marshall.exception.NotImplementedSerializeException;
import giraudsa.marshall.serialisation.Marshaller;
import giraudsa.marshall.serialisation.binary.ActionBinary;

import utils.champ.FakeChamp;
import utils.champ.FieldInformations;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.TreeSet;

@SuppressWarnings("rawtypes")
public class ActionBinaryCollectionType extends ActionBinary<Collection> {


	public ActionBinaryCollectionType() {
		super();
	}

	@Override
	protected void ecritValeur(Marshaller marshaller, Collection obj, FieldInformations fi, boolean isDejaVu) throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, NotImplementedSerializeException, MarshallExeption{
		Type[] types = fi.getParametreType();
		Type genericType = Object.class;
		if(types != null && types.length > 0){
			genericType = types[0];
		}
		FakeChamp fakeChamp = new FakeChamp(null, genericType, fi.getRelation(), fi.getAnnotations());
		
		Deque<Comportement> tmp = new ArrayDeque<>();
		if (!isDejaVu){
			if(strategieSerialiseTout(marshaller, fi))
				setDejaTotalementSerialise(marshaller, obj);
			writeInt(marshaller, obj.size());
			for (Object value : obj) {
				tmp.push(traiteChamp(marshaller, value, fakeChamp));
			}
		}else if(!isDejaTotalementSerialise(marshaller, obj) && strategieSerialiseTout(marshaller, fi)){
			setDejaTotalementSerialise(marshaller, obj);
			for(Object value : obj){
				tmp.push(traiteChamp(marshaller, value, fakeChamp));
			}
		}
		pushComportements(marshaller, tmp);
	}
	
	@Override
	protected Class<?> getTypeObjProblemeHibernate(Object object) {
		Class<?> clazz = object.getClass();
		
		if(clazz.getName().toLowerCase().indexOf("hibernate") != -1){
			if(object.getClass().getName().toLowerCase().indexOf("persistentbag") != -1)
				return ArrayList.class;
			if(object.getClass().getName().toLowerCase().indexOf("persistentset") != -1)
				return HashSet.class;
			if(object.getClass().getName().toLowerCase().indexOf("persistentsortedset") != -1)
				return TreeSet.class;
			else return ArrayList.class;
		}
		return clazz;
	}
}
