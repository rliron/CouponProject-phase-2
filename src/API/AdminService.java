package API;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import exceptionClass.CouponException;
import Classes.Company;
import Classes.Coupon;
import Classes.Customer;
import DataBaseTables.CompanyDBDAO;
import DataBaseTables.CouponDBDAO;
import Facade.AdminFacade;
import Facade.Message;
import Main.CouponSystem;

@Path("/admin")
public class AdminService {
	@Context
	HttpServletRequest req;
	@Context
	HttpServletResponse res;
	
	 HttpSession session ;
	 String idSession;
	CouponSystem system = CouponSystem.getInstance();

	public AdminService() {
	}
	
	@POST
	@Path("/adminLogin")
	@Produces(MediaType.APPLICATION_JSON)
	public String login(@QueryParam("name")String name,@QueryParam("password")String password) {
		AdminFacade facade = (AdminFacade) system
				.login(name, password, "Admin");
		if (facade == null) {
			
			new CouponException("Invalid user");
			System.out.println("problem");
			return "Problem Logging In";
		} else {
			 session = req.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			session = req.getSession(true);
			session.setAttribute("facade", facade);
			System.out.println(session.getId());
			
		}
		try {
			res.sendRedirect("http://localhost:8080/CoupService/Admin.html");
		} catch (IOException e) {
			new Exception ("can't login. try again");
		}
		
		return "Success" ;

	}
	
	@GET
	@Path("/createComp")
	@Produces(MediaType.APPLICATION_JSON)
	public String createComp(@QueryParam("name")String name, @QueryParam("password")String password, @QueryParam("email")String email) throws CouponException{
		session = req.getSession();
//		system.getInstance();
		AdminFacade facade = (AdminFacade) session.getAttribute("facade");
	Company company = new Company();
	company.setCompName(name);
	company.setPassword(password);
	company.setEmail(email);
	company.setID((long) (Math.random()*10001));
	return facade.createCompany(company);
	}
	
	@GET
	@Path("/test")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection test () throws CouponException{
		CouponDBDAO coupon = new CouponDBDAO();
		CompanyDBDAO comp = new CompanyDBDAO();
		session = req.getSession();
		AdminFacade facade = (AdminFacade) session.getAttribute("facade"); 
		Company company1 = new Company();
		Company company2 = new Company();
		company1 = facade.getCompany(1);
		company2 = facade.getCompany(2);
		List <Company>list = new ArrayList<Company>();
			List<Coupon>coupons = new ArrayList<Coupon>();
			company1.setCoupons((List) comp.getCoupons(company1));
			company2.setCoupons((List) comp.getCoupons(company2));
		list.add(company1);
		list.add(company2);
		return list;
		
	}
	
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/removeComp")
	public String removeComp(@QueryParam("id")String id) throws CouponException{
		session = req.getSession();
		String value;
		AdminFacade facade = (AdminFacade) session.getAttribute("facade");
			int CompId = Integer.parseInt(id);
			Company company = facade.getCompany(CompId);
			if(company.getPassword()==null){
				value="No such Company";
			}else{
			facade.removeCompany(company);
		value= "Company removed";
			}
			return value;
		}
		
	
	@PUT
	@Path("/updateComp")
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public String updateCompany(@QueryParam("id")String id,@QueryParam("password")String password, @QueryParam("email")String email ){
		session = req.getSession();
		AdminFacade facade = (AdminFacade) session.getAttribute("facade");
		Company company = new Company();
		try {
			int CompId = Integer.parseInt(id);
			company =facade.getCompany(CompId);
			if (company.getPassword() == null) {
				return "there is no such company";
			}else {
				company.setID(CompId);
				company.setEmail(email);
				company.setPassword(password);
				facade.updateComapny(company);
			}
		} catch (CouponException e) {
			new CouponException("Problem updating the company");
		}
		return "Company updated";	
	}
	
	@GET
	@Path("/getComp")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCompany(@QueryParam("id")String id){
		session = req.getSession();
		Company company = new Company();
		int CompId = Integer.parseInt(id);
		AdminFacade facade = (AdminFacade) session.getAttribute("facade");
		
		try {
			 company = facade.getCompany(CompId);
				if (company.getPassword() == null) {
					return "there is no such company";
				}
		} catch (CouponException e) {
			new CouponException("Problem fetching the company");
		}
		return company.toString();	
	}
//	
	@GET
	@Path("/getAllComp")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection getAllCompanies(){
		session = req.getSession();
		List<Company> list = new ArrayList<Company>();
		AdminFacade facade = (AdminFacade) session.getAttribute("facade");
		try {
			list = (List<Company>) facade.getAllCompanies();
		} catch (CouponException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
//	
//	//-----------------------------------------------------------------
	
	
	@POST
	@Path("/createCust")
	public String createCustomer(@QueryParam("name")String name, @QueryParam("password")String password){
	session = req.getSession();
	AdminFacade facade = (AdminFacade) session.getAttribute("facade");
	Customer customer = new Customer();
	customer.setCustName(name);
	customer.setPassword(password);
	int id= (int) (Math.random()*10001);
		
	try {
		if (facade.getCust(id).getCustName()!=null) {
			customer.setID((long) (Math.random()*10001));
		}else {
			customer.setID(id);
		facade.createCustomer(customer);
		}
	} catch (CouponException e) {
		new CouponException("Problem creating the customer");
	}
	return "Customer created";
	}
	
	@DELETE
	@Path("/removeCust")
	public String removeCustomer(@QueryParam("id")String id) {
		session = req.getSession();
		AdminFacade facade = (AdminFacade) session.getAttribute("facade");
		int CustId = Integer.parseInt(id);
		Customer customer = new Customer();
		try {
		customer = facade.getCust(CustId);
		if(customer.getPassword()==null){
			return "No such Customer";
		}else{
			facade.removeCustomer(customer);
		}
		} catch (CouponException e) {
			new CouponException("Unable to remove customer");
		}
		
		return "Customer removed";
	}
	
	@PUT
	@Path("/updateCust")
	public String updateCustomer(@QueryParam("id")String id, @QueryParam("password")String password){
		session = req.getSession();
		AdminFacade facade = (AdminFacade) session.getAttribute("facade");
		Customer customer = new Customer();
		int ID = Integer.parseInt(id);
		try {
			customer =facade.getCust(ID);
			if (customer.getPassword() == null) {
				return "there is no such customer";
			}else {
				customer.setID(ID);
				customer.setPassword(password);
				facade.updateCustomer(customer);
			}
		} catch (CouponException e) {
			new CouponException("Problem updating the customer");
		}
		return "Customer updated: ";	
	}
	
	@GET
	@Path("/getCust")
	@Produces(MediaType.APPLICATION_JSON)
	public Customer getCustomer(@QueryParam("id")String id){
		session = req.getSession();
		Customer customer = new Customer();
		AdminFacade facade = (AdminFacade) session.getAttribute("facade");
		int ID = Integer.parseInt(id);
		try {
			 customer = facade.getCust(ID);
		} catch (CouponException e) {
			new CouponException("Problem fetching the customer");
		}
		return customer;	
	}
	
	@GET
	@Path("/getAllCust")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Customer> getAllCustomers(){
		session = req.getSession();
		List<Customer> list = new ArrayList<Customer>();
		AdminFacade facade = (AdminFacade) session.getAttribute("facade");
		
		try {
			 list = (List<Customer>) facade.getAllCustomers();
		} catch (CouponException e) {
			new CouponException("Problem fetching all of the customers");
		}
		return list;	
	}
}
