package giraudsa.marshall.serialisation.binary.actions;

import giraudsa.marshall.serialisation.Marshaller;
import giraudsa.marshall.serialisation.binary.ActionBinary;

import utils.champ.FieldInformations;

import java.io.IOException;

public class ActionBinaryStringBuffer extends ActionBinary<StringBuffer> {

	public ActionBinaryStringBuffer() {
		super();
	}
	@Override
	protected void ecritValeur(Marshaller marshaller, StringBuffer sb, FieldInformations fieldInformations) throws IOException {
		if(!isDejaTotalementSerialise(marshaller, sb)){
			setDejaTotalementSerialise(marshaller, sb);
			writeUTF(marshaller, sb.toString());
		}
	}
}