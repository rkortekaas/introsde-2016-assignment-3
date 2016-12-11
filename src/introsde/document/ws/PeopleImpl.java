package introsde.document.ws;

import introsde.document.model.Measure;
import introsde.document.model.Person;

import java.text.ParseException;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

//Service Implementation

@WebService(endpointInterface = "introsde.document.ws.People",
    serviceName="PeopleService")
public class PeopleImpl implements People {

    @Override
    public Person readPerson(int id) {
        System.out.println("---> Reading Person by id = "+id);
        Person p = Person.getPersonById(id);
        if (p!=null) {
            System.out.println("---> Found Person by id = "+id+" => "+p.getName());
        } else {
            System.out.println("---> Didn't find any Person with  id = "+id);
        }
        return p;
    }

    @Override
    public List<Person> readPersonList() {
        return Person.getAll();
    }

    @Override
    public Person createPerson(Person person) {
        Person.savePerson(person);
        return person;
    }

    @Override
    public Person updatePerson(Person person) {
    	return Person.updatePerson(person);
    }

    @Override
    public int deletePerson(int id) {
        Person p = Person.getPersonById(id);
        if (p!=null) {
            Person.removePerson(p);
            return 0;
        } else {
            return -1;
        }
    }
    
    @Override
    public List<Measure> readPersonHistory(int id, String type) {
    	return Measure.getMeasuresByType(id, type);
    }
    
    @Override
    public List<Measure> readMeasureTypes(){
		return Measure.getAll();
    	
    }
    
    @Override
    public Measure readPersonMeasure(int id, String type, int mid){
    	Measure m = Measure.getMeasureById(mid);
    	if (type.equals(m.getType()) && m.getPerson().getIdPerson() == id){
    		return m;
    	}
    	return null;
    }
    
    @Override
    public Measure savePersonMeasure(int id, Measure m) throws ParseException{
    	return Measure.saveMeasure(id, m);
    }
    
    @Override
    public Measure updatePersonMeasure(int id, Measure m){
    	Measure measure = Measure.getMeasureById(m.getIdMeasure());
    	if (measure.getPerson().getIdPerson() == id) {
    		return Measure.updateMeasure(m);
    	}
    	return null;
    }
    
}