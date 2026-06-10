package jp.co.sss.shop.controller.client.user;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jp.co.sss.shop.bean.UserBean;
import jp.co.sss.shop.entity.User;
import jp.co.sss.shop.form.UserForm;
import jp.co.sss.shop.repository.UserRepository;

@Controller
public class ClientUserUpdateController {
	@Autowired
	UserRepository userRepository;
	
	
	//会員詳細画面で変更ボタンを押されたとき、もしくは変更確認画面で戻るを押されたときに現在のデータを保持させて次メソッドへと引き継ぐ
	@RequestMapping(path = "/client/user/update/input", method = RequestMethod.POST)
	public String userUpdateInputPOST(HttpSession session) {
		//会員詳細画面での変更押下時はnullなのでデータベースから初期値を取ってくる
		//会員変更確認画面から戻る押下時はデータがあるためデータベースからの情報は持ってこない
		if(session.getAttribute("userForm") == null) {
			//セッションから現在ログインしている一般会員の情報を取得
			UserBean userBean= (UserBean)session.getAttribute("user");
			//repositoryから条件を検索
			User user = userRepository.findByIdAndDeleteFlag(userBean.getId(), 0);
			//userFormにデータベースから取得した情報をセット
			UserForm userForm = new UserForm();
			BeanUtils.copyProperties(user, userForm);
			//セッションスコープに情報をセット
			session.setAttribute("userForm", userForm);
		}
		return "redirect:/client/user/update/input";
	}
	//セッションスコープから情報を取得し会員変更画面へ遷移するメソッド
	@RequestMapping(path = "/client/user/update/input", method = RequestMethod.GET)
	public String updateInputGet(HttpSession session, Model model) {
		//セッションスコープから入力情報を取得
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		//userFormの情報をリクエストスコープに保存
		model.addAttribute("userForm", userForm);
		return "client/user/update_input";
	}
	
	//会員変更画面で確認ボタン押下時に入力不足をチェックするメソッド
	@RequestMapping(path = "/client/user/update/check", method = RequestMethod.POST)
	public String updateCheckPost(@Valid @ModelAttribute("userForm") UserForm form, BindingResult result, HttpSession session, RedirectAttributes redirectAttributes) {
		UserForm sessionForm = (UserForm) session.getAttribute("userForm");
		//画面から入力された入力フォームをセッションスコープに保存
		session.setAttribute("userForm", form);
		//入力フォームがnullもしくは空の場合、セッションスコープから取得した値をセット
		if (form.getName() == null || form.getName().isEmpty()) {
			form.setName(sessionForm.getName());
		}
		if (form.getEmail() == null || form.getEmail().isEmpty()) {
			form.setEmail(sessionForm.getEmail());
		}
		if (form.getPostalCode() == null || form.getPostalCode().isEmpty()) {
			form.setPostalCode(sessionForm.getPostalCode());
		}
		if (form.getAddress() == null || form.getAddress().isEmpty()) {
			form.setAddress(sessionForm.getAddress());
		}
		if (form.getPhoneNumber() == null || form.getPhoneNumber().isEmpty()) {
			form.setPhoneNumber(sessionForm.getPhoneNumber());
		}
		//パスワードは常に空欄のため一旦コメントアウトしています
		//if (form.getPassword() == null || form.getPassword().isEmpty()) {
			//form.setPassword(sessionForm.getPassword());
		//}
		if (form.getId() == null) {
			form.setId(sessionForm.getId());
		}
		if (form.getAuthority() == null) {
			form.setAuthority(sessionForm.getAuthority());
		}
		if(result.hasErrors()) {
			//入力チェック結果の情報をフラッシュスコープに保存
			redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userForm", result);
			return "redirect:/client/user/update/input";
		}
		return "redirect:/client/user/update/check";
	}
	
	//セッションスコープから情報を取得し会員変更確認画面へ遷移するメソッド
	@RequestMapping(path = "/client/user/update/check", method = RequestMethod.GET)
	public String updateCheckGet(HttpSession session, Model model) {
		//セッションスコープから入力情報を取得
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		//userFormの情報をリクエストスコープに保存
		model.addAttribute("userForm", userForm);
		return "client/user/update_check";
	}
	//会員変更確認画面で登録ボタン押下時にデータベースを更新するメソッド
	@RequestMapping(path = "/client/user/update/complete", method = RequestMethod.POST)
	public String updateCompletePost(HttpSession session) {
		UserForm userForm = (UserForm) session.getAttribute("userForm");
		//データベースにある最新の情報を持ってくる
		User user = userRepository.findById(userForm.getId()).get();
		//セッションスコープ情報をentityへセットする
		user.setId(userForm.getId());
		user.setEmail(userForm.getEmail());
		user.setPassword(userForm.getPassword());
		user.setName(userForm.getName());
		user.setPostalCode(userForm.getPostalCode());
		user.setAddress(userForm.getAddress());
		user.setPhoneNumber(userForm.getPhoneNumber());
		user.setAuthority(userForm.getAuthority());
		//ログインしているuser情報を更新する
		userRepository.save(user);
		return "redirect:/client/user/update/complete";
	}
	//変更完了画面へ遷移するメソッド
	@RequestMapping(path = "/client/user/update/complete", method = RequestMethod.GET)
	public String updateCompleteGet() {
		return "client/user/update_complete";
	}
	
	
	
	
	
	
	
	
}