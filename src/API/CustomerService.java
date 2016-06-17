package API;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import Classes.Company;
import Classes.Coupon;
import DataBaseTables.CouponType;
import Facade.CompanyFacade;
import Facade.CouponClientFacade;
import Facade.CustomerFacade;
import Main.CouponSystem;
import exceptionClass.CouponException;

@Path("/customer")
public class CustomerService {

	@Context
	HttpServletRequest req;
	@Context
	HttpServletResponse res;
	
	 HttpSession session ;
	 String idSession;
	CouponSystem system = CouponSystem.getInstance();
	
	public CustomerService() {
		
	}
	
	@POST
	@Path("/customerLogin")
	@Produces(MediaType.APPLICATION_JSON)
	public String login(@QueryParam("name") String name,@QueryParam("password")String password) {
		CustomerFacade facade = (CustomerFacade) system
				.login(name, password, "Customer");
		if (facade == null) {
			new CouponException("Invalid user");
			System.out.println(name);
			System.out.println("problem");
			return "Problem Logging In";

		} else {
			 session = req.getSession(false);
			if (session != null) {
				session.invalidate();
				System.out.println("destroy");
			}
			session = req.getSession(true);
			session.setAttribute("facade", facade);
			System.out.println(session.getId());
			
		}
		try {
			res.sendRedirect("http://localhost:8080/CoupService/Customer.html");
		} catch (IOException e) {
			new Exception ("can't login. try again");
		}
		return "Success" ;
	}
	
	@POST
	@Path("/purchaseCoupon")
	@Produces(MediaType.APPLICATION_JSON)
	public String purchaseCoupon (@QueryParam ("id") String id) {
		session = req.getSession();
		CustomerFacade facade = (CustomerFacade) session.getAttribute("facade");
		String value = null;
		Coupon coupon = new Coupon();
		int coupId = Integer.parseInt(id);
		coupon.setID(coupId);
		try {
			value=  facade.purchaseCoupon(coupon);
			
		} catch (CouponException e) {
			new CouponException("can't buy the coupon");
		}
		return value;
	}
	
	@GET
	@Path("/getAllPurchased")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Coupon> getAllPurchasedCoupons() {
		List<Coupon> list = new ArrayList<Coupon>();
		session = req.getSession(false);
		CustomerFacade facade = (CustomerFacade) session.getAttribute("facade");
		try {
			list = (List<Coupon>) facade.getAllPurchasedCoupons();
		} catch (CouponException e) {
			new CouponException("problem getting all the purchased coupons");
		}
		return list;
	}
	
	@GET
	@Path("/getAllByType")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Coupon> getAllPurchasedCouponsByType (@QueryParam ("type") String type) {
		session = req.getSession(false);
		List<Coupon> list = new ArrayList<>();
		CustomerFacade facade = (CustomerFacade) session.getAttribute("facade");
		CouponType newType = CouponType.valueOf(type);
		try {
			list = (List<Coupon>) facade.getAllPurchasedCouponsByType(newType);
		} catch (CouponException e) {
			new CouponException("problem getting all the purchased couponsby type");
		}
		return list;
	}
	
	@GET
	@Path("/getAllByPrice")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Coupon> getAllPurchasedCouponsByPrice (@QueryParam ("price") double price) {
		List<Coupon> list = new ArrayList<>();
		CustomerFacade facade = (CustomerFacade) session.getAttribute("facade");
		try {
			list = (List<Coupon>) facade.getAllPurchasedCouponsByPrice(price);
		} catch (CouponException e) {
			new CouponException("problem getting all the purchased couponsby price");
		}
		return list;
	}
	
}
