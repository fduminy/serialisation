package giraudsa.marshall.serialisation.text.xml.actions;

import giraudsa.marshall.serialisation.Marshaller;
import giraudsa.marshall.serialisation.text.xml.ActionXml;
import utils.champ.FieldInformations;

import java.io.IOException;
import java.net.URI;

public class ActionXmlUri extends ActionXml<URI> {

	public ActionXmlUri() {
		super();
	}

	@Override
	protected void ecritValeur(Marshaller marshaller, URI obj, FieldInformations fieldInformations) throws IOException{
		writeEscape(marshaller, obj.toASCIIString());
	}
}