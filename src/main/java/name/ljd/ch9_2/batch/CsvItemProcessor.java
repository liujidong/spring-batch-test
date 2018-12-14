package name.ljd.ch9_2.batch;

import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidationException;

import name.ljd.ch9_2.domain.Person;

public class CsvItemProcessor extends ValidatingItemProcessor<Person> {

	@Override
	public Person process(Person item) throws ValidationException {
		super.process(item);
		
		if(item.getNation().equals("汉族")) {
			item.setNation("01");
		}else {
			item.setNation("02");
		}
		return item;
	}

}
