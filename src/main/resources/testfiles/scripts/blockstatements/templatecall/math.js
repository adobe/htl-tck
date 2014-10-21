/*******************************************************************************
 * Copyright 2014 Adobe Systems Incorporated
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
use(function() {
    var arg1 = this.arg1;
    var arg2 = this.arg2;
    var operation = this.operation;
    switch (this.operation) {
        case 'inc': return arg1 + 1;
        case 'dec': return arg1 - 1;
        case 'add': return arg1 + arg2;
        case 'sub': return arg1 - arg2;
        case 'mult': return arg1 * arg2;
        case 'div': return arg1 / arg2;
        default: throw new Error('Invalid operation: ' + operation);
    }
});
