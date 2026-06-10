package jp.co.sss.shop.controller.client.basket;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.repository.ItemRepository;

@Controller
public class ClientBasketController {

    @Autowired
    private ItemRepository itemRepository; 

//    表示
    @SuppressWarnings("unchecked")
    @GetMapping("/client/basket/list")
    public String viewBasket(HttpSession session, Model model) {
        List<BasketBean> basket = (List<BasketBean>) session.getAttribute("basketBeans");
        if (basket == null) {
            basket = new ArrayList<>();
        }

        List<String> itemNameListZero = new ArrayList<>();
        List<String> itemNameListLessThan = new ArrayList<>();
        List<BasketBean> toRemoveList = new ArrayList<>();

        for (BasketBean bean : basket) {
            Item item = itemRepository.findByIdAndDeleteFlag(bean.getId(), 0);
            
            int currentStock = 0;
            if (item != null) {
                currentStock = item.getStock();
            }
            
            bean.setStock(currentStock);

            if (currentStock == 0) {
                itemNameListZero.add(bean.getName());
                toRemoveList.add(bean);
            } else if (bean.getOrderNum() > currentStock) {
                itemNameListLessThan.add(bean.getName());
                bean.setOrderNum(currentStock); 
            }
        }

        for (BasketBean removeBean : toRemoveList) {
            basket.remove(removeBean);
        }

        if (itemNameListZero.isEmpty() == false) {
            model.addAttribute("itemNameListZero", itemNameListZero);
        }
        if (itemNameListLessThan.isEmpty() == false) {
            model.addAttribute("itemNameListLessThan", itemNameListLessThan);
        }

        if (basket.isEmpty() == true) {
            session.removeAttribute("basketBeans");
        } else {
            session.setAttribute("basketBeans", basket);
        }

        return "client/basket/list";
    }

//    追加
    @SuppressWarnings("unchecked")
    @PostMapping("/client/basket/add")
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
            int newOrderNum = existBean.getOrderNum() + orderNum;
            existBean.setOrderNum(newOrderNum);
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
    
//    削除
    @SuppressWarnings("unchecked")
    @PostMapping("/client/basket/delete")
    public String deleteItem(@RequestParam Integer id, HttpSession session) {
        List<BasketBean> basket = (List<BasketBean>) session.getAttribute("basketBeans");
        if (basket == null) {
            return "redirect:/client/basket/list";
        }

        BasketBean targetBean = null;
        for (BasketBean bean : basket) { 
            if (bean.getId().equals(id)) {
                targetBean = bean;
                break;
            }
        }

        if (targetBean != null) {
            if (targetBean.getOrderNum() > 1) {
                int newOrderNum = targetBean.getOrderNum() - 1;
                targetBean.setOrderNum(newOrderNum);
            } else {
                basket.remove(targetBean);
            }
        }
        
        if (basket.isEmpty() == true) {
            session.removeAttribute("basketBeans");
        } else {
            session.setAttribute("basketBeans", basket);
        }
        
        return "redirect:/client/basket/list";
    }

//    全削除
    @PostMapping("/basket/allDelete")
    public String allDelete(HttpSession session) {
        session.removeAttribute("basketBeans");
        return "redirect:/client/basket/list";
    }
}
