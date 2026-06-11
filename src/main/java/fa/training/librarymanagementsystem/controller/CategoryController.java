package fa.training.librarymanagementsystem.controller;

import fa.training.librarymanagementsystem.dto.request.CreateCategoryRequest;
import fa.training.librarymanagementsystem.dto.response.ApiResponse;
import fa.training.librarymanagementsystem.dto.response.CategoryResponse;
import fa.training.librarymanagementsystem.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Category management. GET is open to any authenticated user; write ops require ADMIN (enforced in SecurityConfig). */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CreateCategoryRequest request) { // Added @Valid here
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", categoryService.createCategory(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id, @Valid @RequestBody CreateCategoryRequest request) { // Added @Valid here
        return ResponseEntity.ok(ApiResponse.success("Category updated", categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
}