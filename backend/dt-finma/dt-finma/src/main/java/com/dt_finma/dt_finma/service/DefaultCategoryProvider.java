package com.dt_finma.dt_finma.service;

import com.dt_finma.dt_finma.model.Category;
import com.dt_finma.dt_finma.model.User;
import com.dt_finma.dt_finma.model.enums.CategoryType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultCategoryProvider {

    public List<Category> buildDefaultCategories(User user) {
        return List.of(
                buildCategory("Alimentacion", CategoryType.EXPENSE, user),
                buildCategory("Transporte", CategoryType.EXPENSE, user),
                buildCategory("Salud", CategoryType.EXPENSE, user),
                buildCategory("Educacion", CategoryType.EXPENSE, user),
                buildCategory("Entretenimiento", CategoryType.EXPENSE, user),
                buildCategory("Servicios", CategoryType.EXPENSE, user),
                buildCategory("Salario", CategoryType.INCOME, user),
                buildCategory("Inversiones", CategoryType.INCOME, user)
        );
    }
    private Category buildCategory(String name, CategoryType type, User user) {
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setDefault(true);
        category.setUser(user);
        return category;
    }

}

