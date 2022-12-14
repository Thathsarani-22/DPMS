package com.example.dpms_research_sample;
import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.dpms_research_sample.CustomUserDetails;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class BST_Controller {
    @Autowired
    private BSTRepository bstrepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private BST_Service service;

    @GetMapping("/BS_Tracker")
    public String BS_TrackerPage() {
        return "BST";
    }
    @GetMapping("/BSTDU")
    public String BSTDUPage(Model model, @CurrentSecurityContext(expression="authentication?.name")String un) {
        un = ((CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getFullName();
        User user= userRepo.findByUsername(un);
        BST bst = new BST();
        bst.setUsername(user);
        model.addAttribute("bst", bst);
       return "BSTDU";
    }

    @PostMapping("/add_update")
    public String processRegister(@ModelAttribute("bst")BST bst,Model model) {
        bstrepo.save(bst);
        System.out.println(bst);
        float BS=bst.getBloodsugar();
        String P=bst.getPeriod();
        if(P.equals("Before Breakfast")||P.equals("Before Lunch")||P.equals("Before Dinner"))
        {
            if(BS<=69.99)
          {
              model.addAttribute("m", "Your Blood Sugar Level is very low.");
          }
            else if (BS>=70.00 && BS<=99.99)
          {
              model.addAttribute("m", "Your Blood Sugar Level is normal.");
          }
            else if (BS>=100 && BS<=125.99)
          {
              model.addAttribute("m", "Your Blood Sugar Level is bit high.");
          }
            else if (BS>=126.00)
          {
              model.addAttribute("m", "Your Blood Sugar Level is very high. Please try to follow healthy life style and have healthy meals.");
          }
        }
        else
        {
            if(BS<=69.99)
            {
                model.addAttribute("m", "Your Blood Sugar Level is very low.");
            }
            else if (BS>=70.00 && BS<140.00)
            {
                model.addAttribute("m", "Your Blood Sugar Level is normal.");
            }
            else if (BS>=140.00 && BS<200.00)
            {
                model.addAttribute("m", "Your Blood Sugar Level is bit high.");
            }
            else if (BS>=200.00)
            {
                model.addAttribute("m", "Your Blood Sugar Level is very high. Please try to follow healthy life style and have healthy meals.");
            }
        }
        return "update_success";
    }
    @GetMapping("/BSTAR")
    public String viewBSTARPage(Model model,@CurrentSecurityContext(expression="authentication?.name")String un){
        un = ((CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getFullName();
        User user= userRepo.findByUsername(un);
        int count=bstrepo.Findcount(user);
        if (count==0)
        {
            return "redirect:/BSTDU";
        }
        else {
            return BSTAllRecordsPage(model, 1, un);
        }
    }

    @GetMapping("/page/{pageNum}")
    public String BSTAllRecordsPage(Model model,@PathVariable(name = "pageNum") int pageNum,String un) {

        un = ((CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getFullName();
        User user= userRepo.findByUsername(un);
        int count=bstrepo.Findcount(user);


            // List<BST> listBST = bstrepo.search(user);
             float sum=bstrepo.FindSum(user);
             float avg=sum/count;

       // model.addAttribute("listBST", listBST);
        model.addAttribute("avg", avg);

            if(avg<126.00)
            {
                model.addAttribute("m", "Your Average Blood Sugar Level is normal");
            }
            else
            {
                model.addAttribute("m", "Your Average Blood Sugar Level is high. Please try to follow healthy life style and have healthy meals.");
            }

            //paging


            Page<BST> page =  service.findlist(pageNum,user);
            List<BST> listBST =page.getContent();
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("totalPages", page.getTotalPages());
            model.addAttribute("totalItems", page.getTotalElements());
            model.addAttribute("listBST", listBST);

        return "/BSTAR";

    }
    @GetMapping("/delete/{id}")
    public String deleteRecord(@PathVariable(name = "id") int id) {
        service.delete(id);
        return "redirect:/BSTAR";
    }
    @GetMapping("/edit/{id}")
    public ModelAndView showEditBTSPage(@PathVariable(name = "id") int id) {
        ModelAndView mav = new ModelAndView("edit_BST");
        BST bst = service.get(id);
        mav.addObject("bst", bst);

        return mav;
    }
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("bst") BST bst) {
        service.save(bst);

        return "redirect:/BSTAR";
    }


    @GetMapping("/BST_pdf")
    public void exportToPDF(HttpServletResponse response,@CurrentSecurityContext(expression="authentication?.name")String un) throws DocumentException, IOException {
        un = ((CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getFullName();
        User user= userRepo.findByUsername(un);

        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=BST_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<BST> listBST = service.listAll(user);

        BST_PDFExporter exporter = new BST_PDFExporter(listBST);
        exporter.export(response);

    }
}
