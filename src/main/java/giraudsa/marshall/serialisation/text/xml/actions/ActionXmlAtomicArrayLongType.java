package giraudsa.marshall.serialisation.text.xml.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicLongArray;

import giraudsa.marshall.exception.MarshallExeption;
import giraudsa.marshall.exception.NotImplementedSerializeException;
import giraudsa.marshall.serialisation.Marshaller;
import giraudsa.marshall.serialisation.text.xml.ActionXml;
import utils.champ.FakeChamp;
import utils.champ.FieldInformations;

public class ActionXmlAtomicArrayLongType  extends ActionXml<AtomicLongArray> {
	
	public ActionXmlAtomicArrayLongType() {
		super();
	}
	
	@Override
	protected void ecritValeur(Marshaller marshaller, AtomicLongArray obj, FieldInformations fi, boolean serialiseTout) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, NotImplementedSerializeException, IOException, MarshallExeption{
		FakeChamp fakeChamp = new FakeChamp("long", Long.class, fi.getRelation(), fi.getAnnotations());
		Deque<Comportement> tmp = new ArrayDeque<>();
		for (int i = 0; i < obj.length(); ++i) {
			tmp.push(traiteChamp(marshaller, obj.get(i), fakeChamp));
		}
		pushComportements(marshaller, tmp);
	}
	
	@Override
	protected void pushComportementParticulier(Marshaller marshaller, AtomicLongArray obj, String nomBalise,
			FieldInformations fieldInformations) {
		if(obj.length() > 0){
			pushComportement(marshaller, newComportementFermeBalise(nomBalise));
			pushComportement(marshaller, newComportementOuvreBaliseEtEcritValeur(obj, nomBalise, fieldInformations));
		}else
			pushComportement(marshaller, newComportementOuvreEtFermeBalise(obj, nomBalise, fieldInformations));
	}
}
