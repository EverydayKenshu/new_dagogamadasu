package jp.co.sss.shop.controller.client.basket;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.repository.ItemRepository;

@Controller
@RequestMapping("/client/basket")
public class ClientBasketController {

    @Autowired
    private ItemRepository itemRepository; 

    
    @SuppressWarnings("unchecked")
    @GetMapping("/list")
    public String viewBasket(HttpSession session, Model model) {
        List<BasketBean> basket = (List<BasketBean>) session.getAttribute("basketBeans");
        if (basket == null) {
            basket = new ArrayList<>();
        }

        List<String> itemNameListLessThan = new ArrayList<>();
        List<String> itemNameListZero = new ArrayList<>();
        List<BasketBean> toRemove = new ArrayList<>();

        for (BasketBean bean : basket) {
        	Item item = itemRepository.findByIdAndDeleteFlag(bean.getId(), 0);
            int currentStock = (item != null) ? item.getStock() : 0; 
            
            bean.setStock(currentStock);

            if (currentStock == 0) {
                itemNameListZero.add(bean.getName());
                toRemove.add(bean);
            } else if (currentStock < bean.getOrderNum()) {
                itemNameListLessThan.add(bean.getName());
                bean.setOrderNum(currentStock); 
            }
        }

        for (BasketBean removeBean : toRemove) {
            basket.remove(removeBean);
        }

        if (!itemNameListZero.isEmpty()) {
            model.addAttribute("itemNameListZero", itemNameListZero);
        }
        if (!itemNameListLessThan.isEmpty()) {
            model.addAttribute("itemNameListLessThan", itemNameListLessThan);
        }

        if (basket.isEmpty()) {
            session.removeAttribute("basketBeans");
        } else {
            session.setAttribute("basketBeans", basket);
        }

        return "client/basket/list";
    }

    /**
     * 2. 買い物かご商品追加
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/add")
    public String addItem(@RequestParam Integer id, @RequestParam Integer orderNum, HttpSession session) {
        List<BasketBean> basket = (List<BasketBean>) session.getAttribute("basketBeans");
        if (basket == null) {
            basket = new ArrayList<>();
        }

        BasketBean existBean = null;
        for (BasketBean bean : basket) {
            if (bean.getId().equals(id)) {
                existBean = bean;
                break;
            }
        }

        if (existBean != null) {
            existBean.setOrderNum(existBean.getOrderNum() + orderNum);
        } else {
        
        	Item item = itemRepository.findByIdAndDeleteFlag(id, 0);
            if (item != null) {
                BasketBean newBean = new BasketBean(id, item.getName(), item.getStock(), orderNum);
                basket.add(0, newBean); 
            }
        }

        session.setAttribute("basketBeans", basket);
        return "redirect:/client/basket/list";
    }
    
    @SuppressWarnings("unchecked")
    @PostMapping("/delete")
    public String deleteItem(@RequestParam Integer id, HttpSession session) {
        List<BasketBean> basket = (List<BasketBean>) session.getAttribute("basketBeans");
        
        if (basket != null) {
            BasketBean targetBean = null;
            for (BasketBean bean : basket) {
                if (bean.getId().equals(id)) {
                    targetBean = bean;
                    break;
                }
            }

            if (targetBean != null) {
                
                if (targetBean.getOrderNum() > 1) {
                    targetBean.setOrderNum(targetBean.getOrderNum() - 1);
                } else {
                    basket.remove(targetBean);
                }
            }
            
            if (basket.isEmpty()) {
                session.removeAttribute("basketBeans");
            } else {
                session.setAttribute("basketBeans", basket);
            }
        }
        return "redirect:/client/basket/list";
    }
    @PostMapping("/allDelete")
    public String allDelete(HttpSession session) {
        session.removeAttribute("basketBeans");
        return "redirect:/client/basket/list";
    }
}