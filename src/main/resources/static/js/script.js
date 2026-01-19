
function approvRemove(id) {
    return confirm("Are you sure?");
}

async function deleteByRow(id) {
    if (!approvRemove()){
        return;
    }

    let response = await fetch(`/home/api/delete/${id}`,
        {
            method:'DELETE'
        });

    if (response.ok){
        let tr = document.querySelector(`tr[data-id='${id}']`);
        if (tr) {
            tr.remove();
        }
        
        // Обновляем таблицу после удаления, чтобы показать актуальные данные
        let input = document.querySelector('#globalFilter');
        if (input && input.value.trim() !== '') {
            // Если был активен поиск, обновляем результаты поиска
            await filterByName();
        } else {
            // Иначе просто обновляем всю таблицу
            await loadAllPeople();
        }
        
        let text = await response.text();
        console.log("Data deleted successfully:", text);
    } else {
        console.error("Failed to delete");
    }
}
let sortMulti = 1;

let sortStates = {};

function sortTable(index) {
    let body = document.getElementsByTagName('tbody')[0];
    let rows = Array.from(body.getElementsByTagName('tr'));
    let headers = document.querySelectorAll('th');

    if (sortStates[index] === undefined)  sortStates[index] = 'asc'

    sortMulti = sortStates[index] === 'asc' ? 1: -1;

    let sortedRows = rows.sort((rowA,rowB) => {
        let cellA = rowA.cells[index].innerText.toLowerCase();
        let cellB = rowB.cells[index].innerText.toLowerCase();

        if (!isNaN(cellA) && !isNaN(cellB))
            return sortMulti * (Number(cellA) - Number(cellB));

        return sortMulti * (cellA.localeCompare(cellB));

    });


    for (const header of headers) {
        header.innerText = header.innerText.split(' ')[0];
    }

    sortedRows.forEach(row=>{
        body.appendChild(row);
    });

    if (sortStates[index] === 'asc'){
        sortStates[index] = 'desc'
        headers[index].innerText += ' ⬇'
    }else{
        sortStates[index] = 'asc'
        headers[index].innerText += ' ⬆'
    }
}


let searchTimeout;

async function filterByName() {
    let input = document.querySelector('#globalFilter');
    let value = input.value.trim();
    let selectedFields = document.querySelector('input[name="searchField"]:checked').value;

    // Очищаем предыдущий таймаут
    clearTimeout(searchTimeout);

    // Если поле поиска пустое, показываем все данные
    if (value === '') {
        await loadAllPeople();
        return;
    }

    // Дебаунс - ждем 300мс после последнего ввода перед поиском
    searchTimeout = setTimeout(async () => {
        try {
            const response = await fetch(`/home/people?query=${encodeURIComponent(value)}&field=${encodeURIComponent(selectedFields)}&format=json`, {
                headers: {
                    'Accept': 'application/json'
                }
            });
            
            if (response.ok) {
                const people = await response.json();
                updateTable(people);
            } else {
                console.error('Search failed:', response.statusText);
            }
        } catch (error) {
            console.error('Error during search:', error);
        }
    }, 300);
}

async function loadAllPeople() {
    try {
        const response = await fetch('/home/people?format=json', {
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (response.ok) {
            const people = await response.json();
            updateTable(people);
        } else {
            console.error('Failed to load people:', response.statusText);
        }
    } catch (error) {
        console.error('Error loading people:', error);
    }
}

function updateTable(people) {
    let tbody = document.getElementsByTagName('tbody')[0];
    
    if (!tbody) {
        console.error('Table body not found');
        return;
    }

    // Очищаем таблицу
    tbody.innerHTML = '';

    // Добавляем найденные записи
    people.forEach(person => {
        let row = document.createElement('tr');
        row.setAttribute('data-id', person.id);
        
        row.innerHTML = `
            <td>${person.id}</td>
            <td>${escapeHtml(person.name || '')}</td>
            <td>${escapeHtml(person.surname || '')}</td>
            <td>${escapeHtml(person.email || '')}</td>
            <td>${person.age || ''}</td>
            <td>
                <a class="dlt btn" onclick="deleteByRow(${person.id})">Delete</a>
                <a class="dlt btn" href="/home/edit/${person.id}">Edit</a>
            </td>
        `;
        
        tbody.appendChild(row);
    });

    // Если результатов нет, показываем сообщение
    if (people.length === 0) {
        let row = document.createElement('tr');
        let cell = document.createElement('td');
        cell.setAttribute('colspan', '6');
        cell.style.textAlign = 'center';
        cell.textContent = 'No results found';
        row.appendChild(cell);
        tbody.appendChild(row);
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
