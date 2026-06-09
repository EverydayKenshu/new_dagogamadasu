package jp.co.sss.shop.controller.client.order;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jp.co.sss.shop.bean.BasketBean;
import jp.co.sss.shop.bean.OrderItemBean;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.Item;
import jp.co.sss.shop.entity.Order;
import jp.co.sss.shop.entity.OrderItem;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.OrderForm;
import jp.co.sss.shop.repository.ItemRepository;
import jp.co.sss.shop.repository.OrderItemRepository;
import jp.co.sss.shop.repository.OrderRepository;
import jp.co.sss.shop.repository.UserRepository;
import jp.co.sss.shop.service.BeanTools;
import jp.co.sss.shop.service.PriceCalc;

@Controller
public class ClientOrderRegistController {

	/**
	 * セッション
	 */
	@Autowired
	HttpSession session;

	@Autowired
	ItemRepository itemRepository;
	
	/**
	 * 注文情報
	 */
	@Autowired
	OrderRepository orderRepository;

	/**
	 * 注文アイテム情報
	 */
	@Autowired
	OrderItemRepository orderItemRepository;

	/**
	 * 会員情報 リポジトリ
	 */
	@Autowired
	UserRepository userRepository;

	/**
	 * Entity、Form、Bean間のデータ生成、コピーサービス
	 */
	@Autowired
	BeanTools beanTools;

	/**
	 * 合計金額計算サービス
	 */
	@Autowired
	PriceCalc priceCalc;

	/**
	 * 住所入力画面　表示処理(POST)
	 * 
	 * @return "redirect:/client/order/address/input" 入力画面　表示
	 */
	@RequestMapping(path = "/client/order/address/input", method = RequestMethod.POST)
	public String registAdress() {

		OrderForm orderForm = (OrderForm) session.getAttribute("orderForm");
		if (orderForm == null) {
			orderForm = new OrderForm();
			// ログインユーザの住所情報を初期セット
			UserBean user = (UserBean) session.getAttribute("user");
			if (user != null) {
				orderForm.setId(user.getId());
				orderForm.setPostalCode(user.getPostalCode());
				orderForm.setAddress(user.getAddress());
				orderForm.setName(user.getName());
				orderForm.setPhoneNumber(user.getPhoneNumber());
			}
			session.setAttribute("orderForm", orderForm);
		}

		return "redirect:/client/order/address/input";
	}

	@RequestMapping(path = "/client/order/address/input", method = RequestMethod.GET)
	public String registAddress(Model model) {

		OrderForm orderForm = (OrderForm) session.getAttribute("orderForm");
		if (orderForm == null) {
			return "redirect:/syserror";
		}

		BindingResult result = (BindingResult) session.getAttribute("result");
		if (result != null) {
			model.addAttribute("org.springframework.validation.BindingResult.orderForm", result);
			session.removeAttribute("result");
		}

		model.addAttribute("orderForm", orderForm);

		return "/client/order/address_input";
	}

	@RequestMapping(path = "/client/order/payment/input", method = RequestMethod.POST)
	public String registPaymentInput(@Valid @ModelAttribute OrderForm form, BindingResult result) {

		OrderForm lastForm = (OrderForm) session.getAttribute("orderForm");
		if (lastForm == null) {
			return "redirect:/syserror";
		}

		// セッションにフォームを保持
		session.setAttribute("orderForm", form);

		if (result.hasErrors()) {
			session.setAttribute("result", result);
			return "redirect:/client/order/address/input";
		}

		return "redirect:/client/order/payment/input";
	}

	@RequestMapping(path = "/client/order/payment/input", method = RequestMethod.GET)
	public String registPaymentInput(Model model) {
		OrderForm orderForm = (OrderForm) session.getAttribute("orderForm");
		if (orderForm == null) {
			return "redirect:/syserror";
		}

		model.addAttribute("orderForm", orderForm);
		return "/client/order/payment_input";
	}

	@RequestMapping(path = "/client/order/check", method = RequestMethod.POST)
	public String registCheck(@ModelAttribute OrderForm form) {
		OrderForm lastForm = (OrderForm) session.getAttribute("orderForm");
		if (lastForm == null) {
			return "redirect:/syserror";
		}

		// 支払方法等を保持して確認画面へ
		lastForm.setPayMethod(form.getPayMethod());
		session.setAttribute("orderForm", lastForm);

		return "redirect:/client/order/check";
	}

	@RequestMapping(path = "/client/order/check", method = RequestMethod.GET)
	public String registCheck(Model model) {
		OrderForm orderForm = (OrderForm) session.getAttribute("orderForm");

		@SuppressWarnings("unchecked")
		List<BasketBean> basket = (List<BasketBean>) session.getAttribute("basket");
		
		if (orderForm == null || basket == null || basket.isEmpty()) {
			return "redirect:/client/basket/list";
		}

		List<OrderItemBean> orderItemBeanList = new ArrayList<OrderItemBean>();
		boolean stockChanged = false;

		for (BasketBean b : basket) {
			Optional<Item> itemOpt = itemRepository.findById(b.getId());
			if (itemOpt.isEmpty()) {
				// 買い物かごに何もない場合
				continue;
			}
			Item item = itemOpt.get();
			if (item.getStock() == 0) {
				// 在庫なしの場合は削除
				stockChanged = true;
				continue;
			}
			if (item.getStock() < b.getOrderNum()) {
				// 在庫不足の場合は注文数を在庫に合わせる
				b.setOrderNum(item.getStock());
				stockChanged = true;
			}
			orderItemBeanList.add(beanTools.generateOrderItemBean(item, b));
		}

		if (stockChanged) {
			// セッションのバスケットを更新
			session.setAttribute("basket", basket);
		}

		int total = priceCalc.orderItemBeanPriceTotalUseSubtotal(orderItemBeanList);

		model.addAttribute("orderForm", orderForm);
		model.addAttribute("orderItemBeanList", orderItemBeanList);
		model.addAttribute("total", total);

		return "/client/order/check";
	}

	@RequestMapping(path = "/client/order/complete", method = RequestMethod.POST)
	@Transactional
	public String registComplete() {

		OrderForm orderForm = (OrderForm) session.getAttribute("orderForm");
		@SuppressWarnings("unchecked")
		List<BasketBean> basket = (List<BasketBean>) session.getAttribute("basket");

		if (orderForm == null || basket == null || basket.isEmpty()) {
			return "redirect:/client/basket/list";
		}

		// 在庫の最終チェック
		for (BasketBean b : basket) {
			Optional<Item> itemOpt = itemRepository.findById(b.getId());
			if (itemOpt.isEmpty()) {
				return "redirect:/client/order/check";
			}
			Item item = itemOpt.get();
			if (item.getStock() < b.getOrderNum()) {
				// 在庫不足
				return "redirect:/client/order/check";
			}
		}

		// 注文エンティティ作成
		Order order = new Order();
		order.setPostalCode(orderForm.getPostalCode());
		order.setAddress(orderForm.getAddress());
		order.setName(orderForm.getName());
		order.setPhoneNumber(orderForm.getPhoneNumber());
		order.setPayMethod(orderForm.getPayMethod());

		// 会員情報セット
		UserBean userBean = (UserBean) session.getAttribute("user");
		if (userBean != null) {
			User user = userRepository.findById(userBean.getId()).orElse(null);
			order.setUser(user);
		}

		orderRepository.save(order);

		// 注文商品登録・在庫更新
		for (BasketBean b : basket) {
			Item item = itemRepository.findById(b.getId()).orElse(null);
			if (item == null) {
				continue;
			}

			OrderItem orderItem = new OrderItem();
			orderItem.setItem(item);
			orderItem.setOrder(order);
			orderItem.setPrice(item.getPrice());
			orderItem.setQuantity(b.getOrderNum());

			orderItemRepository.save(orderItem);

			// 在庫減少
			item.setStock(item.getStock() - b.getOrderNum());
			itemRepository.save(item);
		}

		// セッション削除
		session.removeAttribute("orderForm");
		session.removeAttribute("basket");

		return "redirect:/client/order/complete";
	}

	@RequestMapping(path = "/client/order/complete", method = RequestMethod.GET)
	public String registCompleteFinish() {
		return "/client/order/complete";
	}

}
