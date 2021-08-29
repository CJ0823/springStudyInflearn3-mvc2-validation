package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import hello.itemservice.web.validation.form.ItemSaveForm;
import hello.itemservice.web.validation.form.ItemUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
@Slf4j
public class ValidationItemControllerV4 {

  private final ItemRepository itemRepository;

  @GetMapping
  public String items(Model model) {
    List<Item> items = itemRepository.findAll();
    model.addAttribute("items", items);
    return "/validation/v4/items";
  }

  @GetMapping("/{itemId}")
  public String item(@PathVariable long itemId, Model model) {
    Item item = itemRepository.findById(itemId);
    model.addAttribute("item", item);
    return "/validation/v4/item";
  }

  @GetMapping("/add")
  public String addForm(Model model) {
    model.addAttribute("item", new Item());
    return "/validation/v4/addForm";
  }

  @PostMapping("/add")
  public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

    Item item = new Item();
    item.setItemName(form.getItemName());
    item.setPrice(form.getPrice());
    item.setQuantity(form.getQuantity());


    //글로벌 검증
    if (form.getPrice() != null && form.getQuantity() != null) {
      int totalPrice = form.getQuantity() * form.getPrice();
      if (totalPrice < 10000) {
        bindingResult.reject("totalPriceMin", new Object[]{10000, totalPrice}, null);
      }
    }

    log.info("bindingResult ={}", bindingResult);
    //검증 로직 실패 시, 다시 입력 폼으로
    //이중 부정이므로 리팩토링으로 hasError 등으로 바꾸는게 좋다.
    if (bindingResult.hasErrors()) {
      return "validation/v4/addForm";
    }

    //검증 로직 통과 시, 상품 저장
    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v4/items/{itemId}";
  }


  @GetMapping("/{itemId}/edit")
  public String editForm(@PathVariable Long itemId, Model model) {
    Item item = itemRepository.findById(itemId);
    model.addAttribute("item", item);
    return "validation/v4/editForm";
  }

  @PostMapping("/{itemId}/edit")
  public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {

    Item item = new Item();
    item.setId(form.getId());
    item.setItemName(form.getItemName());
    item.setPrice(form.getPrice());
    item.setQuantity(form.getQuantity());

    //글로벌 검증
    if (item.getPrice() != null && item.getQuantity() != null) {
      int totalPrice = item.getQuantity() * item.getPrice();
      if (totalPrice < 10000) {
        bindingResult.reject("totalPriceMin", new Object[]{10000, totalPrice}, null);
      }
    }

    log.info("bindingResult ={}", bindingResult);
    if (bindingResult.hasErrors()) {
      return "validation/v4/editForm";
    }

    itemRepository.update(itemId, item);
    return "redirect:/validation/v4/items/{itemId}";
  }

}

