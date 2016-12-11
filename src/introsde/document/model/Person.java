package introsde.document.model;

import introsde.document.dao.LifeCoachDao;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.*;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Entity  // indicates that this class is an entity to persist in DB
@Table(name="\"Person\"") // to whate table must be persisted

@NamedQuery(name="Person.findAll", query="SELECT p FROM Person p") //utilizzata sotto nelle query
@XmlRootElement
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id // defines this attributed as the one that identifies the entity
    //@GeneratedValue(strategy=GenerationType.AUTO) 
    @GeneratedValue(generator="sqlite_person")
    @TableGenerator(name="sqlite_person", table="sqlite_sequence",
        pkColumnName="name", valueColumnName="seq",
        pkColumnValue="Person")
    @Column(name="\"idPerson\"") // maps the following attribute to a column
    private int idPerson;
    
    @Column(name="\"lastname\"")
    private String lastname;
    
    @Column(name="\"name\"")
    private String name;
    
    @Temporal(TemporalType.DATE) // defines the precision of the date attribute
    @Column(name="\"birthdate\"")
    private Date birthdate; 
    
    @Column(name="\"email\"")
    private String email;
    
    // mappedBy must be equal to the name of the attribute in Measure that maps this relation
    @OneToMany(mappedBy="person",cascade=CascadeType.ALL,fetch=FetchType.EAGER)
    private List<Measure> healthHistory; // all measurements
        
    
    @Transient
    private List<Measure> currentHealth; // one for each type of measure
    
    @XmlElementWrapper(name = "currentHealth")
    @XmlElement(name = "measure")
    public List<Measure> getCurrentHealth() {
    	if(currentHealth==null)
    		this.setCurrentHealth(getList(this.idPerson));
        return currentHealth;
    }
    
    public void setCurrentHealth(List<Measure> ch) {
        this.currentHealth = ch;
    }
    
    public static List<Measure> getList(long id){
    	EntityManager em = LifeCoachDao.instance.createEntityManager();
    	List<Measure> list = em
				.createQuery("SELECT p FROM Measure p WHERE p.person.idPerson = :id ORDER BY p.type", Measure.class)
				.setParameter("id", id).getResultList();
	    LifeCoachDao.instance.closeConnections(em);
	    return list;
	    
//	    Map<String, Measure> currentStatus = new HashMap<>();
//    	for (int i = 0; i<list.size(); i++){
//    		String key = list.get(i).getType();
//    		if (!currentStatus.containsKey(key)){
//    			currentStatus.put(key, list.get(i));
//    		}else {
//    			Date d1 = list.get(i).getDateRegistered();
//    			Date d2 = currentStatus.get(key).getDateRegistered();
//    			if(d1.after(d2)){
//    				currentStatus.remove(key);
//    				currentStatus.put(key, list.get(i));
//    			}	
//    		}
//    	}
//    	
//    	List<Measure> l = new ArrayList<>();
//    	for (Measure m : currentStatus.values()){
//    		l.add(m);
//    	}
//    	return l;
    	
    }
    
    // the GETTERS and SETTERS of all the private attributes
    public int getIdPerson() {
		return idPerson;
	}

	public void setIdPerson(int idPerson) {
		this.idPerson = idPerson;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getBirthdate(){
    	if(this.birthdate == null) {
    	      return null;
    	}
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(this.birthdate);
    }

    public void setBirthdate(String bd) throws ParseException{
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        Date date = format.parse(bd);
        this.birthdate = date;
    }

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	@XmlTransient
	public List<Measure> getHealthHistory() {
		if (this.healthHistory.isEmpty() && !this.getCurrentHealth().isEmpty()){
			this.setHealthHistory(this.getCurrentHealth());
		}
			
		return this.healthHistory;
	}

	public void setHealthHistory(List<Measure> healthHistory) {
		this.healthHistory = healthHistory;
	}
	
	
	// QUERYING TO THE DATABASE
	
	public static Person getPersonById(int personId) {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        Person p = em.find(Person.class, personId);
        LifeCoachDao.instance.closeConnections(em);
        return p;
    }

    public static List<Person> getAll() {
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        List<Person> list = em.createNamedQuery("Person.findAll", Person.class)
            .getResultList();
        LifeCoachDao.instance.closeConnections(em);
        return list;
    }

    public static Person savePerson(Person p) {
    	List<Measure> m = p.getCurrentHealth();
    	p.setCurrentHealth(null);
    	EntityManager em = LifeCoachDao.instance.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(p);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
        for(int i = 0;i<m.size();i++){
        	m.get(i).setPerson(p);
        	Measure.updateMeasure(m.get(i));
        }
        return p;
    } 

    public static Person updatePerson(Person p) {
    	System.out.println(p);
    	List<Measure> m = Person.getPersonById(p.getIdPerson()).getHealthHistory();
    	p.setHealthHistory(m);
        EntityManager em = LifeCoachDao.instance.createEntityManager(); 
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        p=em.merge(p);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
        return p;
    }

    public static void removePerson(Person p) {
    	List<Measure> list = p.getHealthHistory();
    	for(Measure m : list){
        	Measure.removeMeasure(m);
        }
        EntityManager em = LifeCoachDao.instance.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        p=em.merge(p);
        em.remove(p);
        tx.commit();
        LifeCoachDao.instance.closeConnections(em);
        
    }
    
    
    

}