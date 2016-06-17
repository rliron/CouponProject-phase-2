package API;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import Classes.Coupon;
import DataBaseTables.CouponType;
import Facade.CompanyFacade;
import Main.CouponSystem;
import exceptionClass.CouponException;
@Path("/company")
public class CompanyService {
	@Context
	private HttpServletRequest req;
	
	HttpSession session;
	@Context
	HttpServletResponse res;
	
	CouponSystem system = CouponSystem.getInstance();
	private static final String SERVER_UPLOAD_LOCATION_FOLDER = "/Coupon/";
	public CompanyService() {
	}
	
	
	@POST
	@Path("/companyLogin")
	@Produces(MediaType.APPLICATION_JSON)
	public String login(@QueryParam("name")String name,@QueryParam("password")String password) {
		CompanyFacade facade =  (CompanyFacade) system
				.login(name, password, "Company");
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
			res.sendRedirect("http://localhost:8080/CoupService/Company.html");
		} catch (IOException e) {
			new Exception ("can't login. try again");
		}
		
		return "Success" ;

	}
	
	@POST
	@Path("/createCoup")
	@Produces(MediaType.APPLICATION_JSON)
	public String createCoup(@QueryParam("title") String title,
			@QueryParam("endDate") String endDate,
			@QueryParam("startDate") String startDate,
			@QueryParam("amount") String amount,
			@QueryParam("message") String message,
			@QueryParam("image") String image,
			@QueryParam("price") String price,
			@QueryParam("type") String type) throws CouponException {
		session = req.getSession();
		CompanyFacade facade = (CompanyFacade) session.getAttribute("facade");
		int kamut = Integer.parseInt(amount);
		double newPrice = Double.parseDouble(price);
		CouponType newType = CouponType.valueOf(type);
		Coupon coupon = new Coupon();
		coupon.setID((long) (Math.random()*10001));
		coupon.setAmount(kamut);
		coupon.setEndDate(endDate);
		coupon.setMessage(message);
		coupon.setImage(image);
		coupon.setPrice(newPrice);
		coupon.setStartDate(startDate);
		coupon.setTitle(title);
		coupon.setType(newType);
		return	facade.createCoupon(coupon);
	}
	
	@POST
	@Path("/image")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(
			@FormDataParam("imageData") InputStream fileInputStream,
			@FormDataParam("imageData") FormDataContentDisposition contentDispositionHeader) {
		String filePath = SERVER_UPLOAD_LOCATION_FOLDER + 
				contentDispositionHeader.getFileName();
		System.out.println("been here");
		saveFile (fileInputStream, filePath);  //save the file to the server
		return Response.status(200).entity(filePath).build();
	}
	//save upload file to a defined location on the server
	private void saveFile (InputStream uploadedInputStream, String serverLocation) {
		try {
			OutputStream outputStream = new FileOutputStream(new File(serverLocation));
			int read = 0;
			byte[] bytes = new byte[1024];
			
			outputStream = new FileOutputStream(new File(serverLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.flush();
			outputStream.close();
		}catch (IOException e) {
			new CouponException("problem uploading the image");
		}
	}
	
	@DELETE
	@Path("/removeCoup")
	@Produces(MediaType.APPLICATION_JSON)
	public String removeCoup(@QueryParam("id")String id) throws CouponException{
		session = req.getSession();
		CompanyFacade facade = (CompanyFacade) session.getAttribute("facade");
		Coupon coupon = new Coupon();
		int newID = Integer.parseInt(id);
		coupon=facade.getCoupon(newID);
		try {
			if(coupon.getTitle()==null){
				return "No such coupon";
			}else
				facade.removeCoupon(coupon);
		} catch (CouponException e) {
			new CouponException("Couldnt remove coupon");
		}
		return "Coupon succesfully been removed";
		}
	
	
	@PUT
	@Path("/updateCoup")
	@Produces(MediaType.APPLICATION_JSON)
	public String updateCoupon(
			@QueryParam("endDate") String endDate,
			@QueryParam("price") String price,@QueryParam("id") String id) throws CouponException{
		session = req.getSession(false);
		CompanyFacade facade = (CompanyFacade) session.getAttribute("facade");
		double newPrice = Double.parseDouble(price);
		int newID = Integer.parseInt(id);
		Coupon coupon = new Coupon();
		coupon.setID(newID);
		coupon.setEndDate(endDate);
		coupon.setPrice(newPrice);
		
		return facade.updateCoupon(coupon);	
	}
	
	@GET
	@Path("/getCoup")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCoupon(@QueryParam("id")String id) throws CouponException{
		Coupon coupon = new Coupon();
		session = req.getSession(false);
		CompanyFacade facade = (CompanyFacade) session.getAttribute("facade");
		int newID = Integer.parseInt(id);
		coupon = facade.getCoupon(newID);
		return coupon.toString();	
	}
	
	@GET
	@Path("/getAllCoup")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection getAllCoupons() throws CouponException{
		List<Coupon> list = new ArrayList<Coupon>();
		session = req.getSession(false);
		CompanyFacade facade = (CompanyFacade) session.getAttribute("facade");
		list = (List<Coupon>) facade.getAllCoupon();
		return list;	
	}
	
	@GET
	@Path("/getCoupByType")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Coupon> getCouponByType(@QueryParam("type")String type) throws CouponException{
		List<Coupon> listByType = new ArrayList<Coupon>();
		session = req.getSession(false);
		CompanyFacade facade = (CompanyFacade) session.getAttribute("facade");
		CouponType newType = CouponType.valueOf(type);
		listByType=(List<Coupon>) facade.getCouponByType(newType);
		return listByType;	
	}
}
