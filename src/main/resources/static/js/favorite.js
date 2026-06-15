// お気に入り変更内容を保存する連想配列を作成する
const favoriteChanges = {};

// コンテキストパスを取得する
const contextPath = "/" + location.pathname.split("/")[1];

// ハート画像をクリックした時の処理
function toggleFavorite(img) {

	// 商品IDを取得する
	const itemId = img.dataset.itemId;

	// 赤ハートの場合
	if (img.src.includes("red.png")) {

		// 灰色ハートに変更する
		img.src = contextPath + "/images/gray.png";

		// お気に入り解除状態を保存する
		favoriteChanges[itemId] = 0;

	// 灰色ハートの場合
	} else {

		// 赤ハートに変更する
		img.src = contextPath + "/images/red.png";

		// お気に入り登録状態を保存する
		favoriteChanges[itemId] = 1;
	}

	// hiddenに変更内容を保存する
	setFavoriteChangesHidden();
}

// hiddenへお気に入り変更内容を保存する処理
function setFavoriteChangesHidden() {

	// favoriteChanges用hiddenを取得する
	const favoriteInput = document.getElementById("favoriteChanges");

	// hiddenが存在しない場合は処理を終了する
	if (!favoriteInput) {
		return;
	}

	// 変更内容をJSON文字列にしてhiddenへ保存する
	favoriteInput.value = JSON.stringify(favoriteChanges);
}

// フォーム送信前にお気に入り変更内容をコピーする処理
function copyFavoriteChanges(button) {

	// 押されたボタンの親フォームを取得する
	const form = button.closest("form");

	// 画面全体のhiddenを取得する
	const favoriteInput = document.getElementById("favoriteChanges");

	// hiddenがある場合は値を取得し、ない場合は空文字にする
	const favoriteValue = favoriteInput ? favoriteInput.value : "";

	// フォーム内のhiddenへ値を設定する
	form.querySelector(".favoriteChangesFormValue").value = favoriteValue;
}

// カテゴリ検索時に保存後の遷移先を設定する処理
function setCategorySearchRedirect(form, sortType) {

	// 選択されたカテゴリIDを取得する
	const categoryId = form.querySelector("[name='categoryId']").value;

	// 画面全体のhiddenを取得する
	const favoriteInput = document.getElementById("favoriteChanges");

	// hiddenがある場合は値を取得し、ない場合は空文字にする
	const favoriteValue = favoriteInput ? favoriteInput.value : "";

	// フォーム内のお気に入り変更hiddenへ値を設定する
	form.querySelector(".favoriteChangesFormValue").value = favoriteValue;

	// 検索後の遷移先URLを作成する
	const redirectUrl = contextPath + "/client/item/list/" + sortType + "?categoryId=" + categoryId;

	// フォーム内の遷移先hiddenへURLを設定する
	form.querySelector(".redirectUrlValue").value = redirectUrl;
}

// 画面読み込み完了後に実行する
document.addEventListener("DOMContentLoaded", function () {

	// 保存対象リンクをすべて取得する
	const links = document.querySelectorAll(".favorite-save-link");

	// リンクを1件ずつ処理する
	links.forEach(function (link) {

		// リンククリック時の処理を設定する
		link.addEventListener("click", function (event) {

			// 通常のリンク遷移を止める
			event.preventDefault();

			// 遷移先URLを取得する
			const redirectUrl = link.href;

			// 画面全体のhiddenを取得する
			const favoriteInput = document.getElementById("favoriteChanges");

			// hiddenがある場合は値を取得し、ない場合は空文字にする
			const favoriteValue = favoriteInput ? favoriteInput.value : "";

			// 送信用フォームを作成する
			const form = document.createElement("form");

			// POST送信に設定する
			form.method = "post";

			// 保存専用URLを送信先に設定する
			form.action = contextPath + "/client/favorite/save-and-redirect";

			// お気に入り変更用hiddenを作成する
			const favoriteHidden = document.createElement("input");

			// hidden項目に設定する
			favoriteHidden.type = "hidden";

			// パラメータ名を設定する
			favoriteHidden.name = "favoriteChanges";

			// お気に入り変更内容を設定する
			favoriteHidden.value = favoriteValue;

			// hiddenをフォームに追加する
			form.appendChild(favoriteHidden);

			// 遷移先URL用hiddenを作成する
			const redirectHidden = document.createElement("input");

			// hidden項目に設定する
			redirectHidden.type = "hidden";

			// パラメータ名を設定する
			redirectHidden.name = "redirectUrl";

			// 遷移先URLを設定する
			redirectHidden.value = redirectUrl;

			// hiddenをフォームに追加する
			form.appendChild(redirectHidden);

			// フォームをbodyに追加する
			document.body.appendChild(form);

			// フォームを送信する
			form.submit();
		});
	});
});

// カテゴリ検索フォーム送信前に呼ばれる関数をwindowに登録する
window.setCategorySearchRedirect = function (form, sortType) {

	// 選択されたカテゴリIDを取得する
	const categoryId = form.querySelector("[name='categoryId']").value;

	// お気に入り変更内容を保持するhiddenを取得する
	const favoriteInput = document.getElementById("favoriteChanges");

	// hiddenがある場合は値を取得し、ない場合は空文字にする
	const favoriteValue = favoriteInput ? favoriteInput.value : "";

	// フォーム内のお気に入り変更hiddenへ値を設定する
	form.querySelector(".favoriteChangesFormValue").value = favoriteValue;

	// 遷移先URLを作成する
	const redirectUrl =
	"/client/item/list/" +sortType +"?categoryId=" +categoryId;

	// フォーム内の遷移先URLhiddenへ値を設定する
	form.querySelector(".redirectUrlValue").value = redirectUrl;

	// フォーム送信を続行する
	return true;
};